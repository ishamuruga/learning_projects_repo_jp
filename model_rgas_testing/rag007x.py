from langchain_aws import ChatBedrockConverse, BedrockEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_core.documents import Document
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from dotenv import load_dotenv

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
# Query
# =============================
query = "What is RAG?"
response = rag_chain.invoke(query)

print("\nAnswer:\n", response)