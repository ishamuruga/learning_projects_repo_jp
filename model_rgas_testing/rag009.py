# =============================
# HARD FIX: Patch missing VertexAI dependency BEFORE ragas import
# =============================
import sys
import types

# Create full module chain
vertexai_module = types.ModuleType("vertexai")
chat_models_module = types.ModuleType("chat_models")

# Dummy class to satisfy import
class ChatVertexAI:
    pass

chat_models_module.vertexai = vertexai_module
vertexai_module.ChatVertexAI = ChatVertexAI

# Register modules properly
sys.modules["langchain_community.chat_models"] = chat_models_module
sys.modules["langchain_community.chat_models.vertexai"] = vertexai_module

###############################################################################

from langchain_aws import ChatBedrockConverse, BedrockEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_core.documents import Document
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from dotenv import load_dotenv
import pandas as pd
from datasets import Dataset
from ragas import evaluate
from ragas.llms import LangchainLLMWrapper
from ragas.embeddings import LangchainEmbeddingsWrapper
# from ragas.metrics.collections import AnswerRelevancy, ContextPrecision, Faithfulness
from ragas.metrics import answer_relevancy, context_precision, faithfulness

# Load .env
load_dotenv()



# =============================
# LLM (ChatBedrockConverse)
# =============================
llm = ChatBedrockConverse(
    model_id="us.amazon.nova-pro-v1:0",
    region_name="us-east-1",
    temperature=0,
    max_tokens=500
)

# =============================
# Embeddings
# =============================
embeddings = BedrockEmbeddings(
    model_id="amazon.titan-embed-text-v2:0",
    region_name="us-east-1"
)

# =============================
# Documents
# =============================
docs = [
    Document(page_content="RAG stands for Retrieval Augmented Generation."),
    Document(page_content="RAG combines retrieval with LLM generation."),
    Document(page_content="LangChain helps build RAG pipelines easily.")
]

# =============================
# Vector Store
# =============================
vectorstore = FAISS.from_documents(docs, embeddings)
retriever = vectorstore.as_retriever(search_kwargs={"k": 2})

# =============================
# Prompt
# =============================
prompt = ChatPromptTemplate.from_template("""
Answer the question based only on the context below.

Context:
{context}

Question:
{question}
""")

# =============================
# RAG Pipeline (LCEL)
# =============================
def format_docs(docs):
    return "\n\n".join(doc.page_content for doc in docs)

rag_chain = (
    {
        "context": retriever | format_docs,
        "question": lambda x: x
    }
    | prompt
    | llm
    | StrOutputParser()
)

# =============================
# RAGAS Evaluation
# =============================

# Test dataset
test_questions = [
    "What is RAG?",
    "How does RAG combine retrieval with generation?",
    "What does LangChain help with?"
]

# Run RAG pipeline on test questions and collect results
evaluation_data = []

for question in test_questions:
    # Get retrieved contexts
    retrieved_docs = retriever.invoke(question)
    contexts = [doc.page_content for doc in retrieved_docs]
    
    # Get answer from RAG chain
    answer = rag_chain.invoke(question)
    
    # Add to evaluation data
    evaluation_data.append({
        "question": question,
        "answer": answer,
        "contexts": contexts,
        "ground_truth": "Reference answer for comparison"  # Optional: add reference answers
    })

# Convert to Dataset
df = pd.DataFrame(evaluation_data)
dataset = Dataset.from_pandas(df, preserve_index=False)

# Setup RAGAS evaluator LLM and embeddings
evaluator_llm = LangchainLLMWrapper(
    ChatBedrockConverse(
        model_id="us.amazon.nova-pro-v1:0",
        region_name="us-east-1",
        temperature=0.7,
    )
)

evaluator_embeddings = LangchainEmbeddingsWrapper(
    BedrockEmbeddings(
        model_id="amazon.titan-embed-text-v2:0",
        region_name="us-east-1"
    )
)

# Run RAGAS evaluation
print("\n" + "="*50)
print("RAGAS Evaluation Results")
print("="*50)

# result = evaluate(
#     dataset=dataset,
#     metrics=[
#         Faithfulness(llm=evaluator_llm),
#         AnswerRelevancy(llm=evaluator_llm, embeddings=evaluator_embeddings),
#         ContextPrecision(llm=evaluator_llm),
#     ],
#     llm=evaluator_llm,
#     embeddings=evaluator_embeddings,
#     show_progress=True,
# )

result = evaluate(
    dataset=dataset,
    metrics=[
        faithfulness,
        answer_relevancy,
        context_precision
    ],
    llm=evaluator_llm,
    embeddings=evaluator_embeddings,
    show_progress=True,
    raise_exceptions=False
)

print("\nMetrics Scores:")
print("\nMetrics Scores:")
print(result)

# for metric_name in result.keys():
#     print(f"  {metric_name}: {result[metric_name]:.4f}")

# =============================
# Example RAG Query
# =============================
print("\n" + "="*50)
print("Example RAG Query")
print("="*50)
query = "What is RAG?"
response = rag_chain.invoke(query)
print(f"\nQuestion: {query}")
print(f"Answer: {response}")