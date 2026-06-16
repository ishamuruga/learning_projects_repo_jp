from deepeval.models import DeepEvalBaseLLM
from langchain_aws import ChatBedrock

class BedrockLLM(DeepEvalBaseLLM):
    def __init__(self):
        self.model = None
        self.load_model()

    def load_model(self):
        self.model = ChatBedrock(
            model_id="us.amazon.nova-pro-v1:0",
            region_name="us-east-1",
            model_kwargs={
                "temperature": 0,
                "max_tokens": 512
            }
        )

    def generate(self, prompt: str) -> str:
        response = self.model.invoke(prompt)
        return response.content

    async def a_generate(self, prompt: str) -> str:
        # DeepEval sometimes uses async
        return self.generate(prompt)

    def get_model_name(self) -> str:
        return "bedrock-nova-pro"


# from deepeval.models import DeepEvalBaseLLM
# from langchain_aws import ChatBedrock

# # class BedrockLLM(DeepEvalBaseLLM):
# #     def __init__(self):
# #         self.model = ChatBedrock(
# #             model_id="us.amazon.nova-pro-v1:0",
# #             region_name="us-east-1"
# #         )

# #     def generate(self, prompt: str) -> str:
# #         response = self.model.invoke(prompt)
# #         return response.content

# #     # def generate(self, prompt: str) -> str:
# #     #     response = self.model.invoke(prompt)
# #     #     return response.content

# #     async def a_generate(self, prompt: str) -> str:
# #         # DeepEval sometimes uses async
# #         return self.generate(prompt)

# #     def get_model_name(self) -> str:
# #         return "us.amazon.nova-pro-v1:0"