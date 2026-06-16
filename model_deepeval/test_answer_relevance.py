from deepeval import assert_test
from deepeval.test_case import LLMTestCase
from deepeval.metrics import AnswerRelevancyMetric, FaithfulnessMetric
from bedrockllm import BedrockLLM

def test_answer_relevancy():
    print("Testing answer relevancy...")
    answer_relevancy_metric = AnswerRelevancyMetric(
        threshold=0.5,
        model=BedrockLLM()
    )
    test_case = LLMTestCase(
        input="What if these shoes don't fit?",
        actual_output="We offer a 30-day full refund at no extra cost."
    )
    assert_test(test_case, [answer_relevancy_metric])

def test_answer_relevancy_fail():
    metric = AnswerRelevancyMetric(threshold=0.5,model=BedrockLLM())

    test_case = LLMTestCase(
        input="What if these shoes don't fit?",
        actual_output="Our stores are open from 9 AM to 9 PM every day."
    )

    assert_test(test_case, [metric])

def test_hallucination_fail():
    metric = FaithfulnessMetric(threshold=0.7,model=BedrockLLM())

    test_case = LLMTestCase(
        input="Who invented the telephone?",
        actual_output="Alexander Graham Bell invented the telephone in 1876 and later became the president of the United States.",
        retrieval_context=[
            "Alexander Graham Bell invented the telephone in 1876."
        ]
    )

    assert_test(test_case, [metric])

def test_hallucination_pass():
    metric = FaithfulnessMetric(threshold=0.7,model=BedrockLLM())

    test_case = LLMTestCase(
        input="Who invented the telephone?",
        actual_output="Alexander Graham Bell invented the telephone in 1876.",
        retrieval_context=[
            "Alexander Graham Bell invented the telephone in 1876."
        ]
    )

    assert_test(test_case, [metric])