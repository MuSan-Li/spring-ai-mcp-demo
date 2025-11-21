package cn.saa.demo;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class SpringAiAgentsDemoApplicationTests {

	@Test
	void contextLoads() {
		RestTemplate restTemplate = new RestTemplate();

		// 设置请求头
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// 设置请求体
		String requestBody = """
				{
				  "page_number": 1,
				  "page_size": 1,
				  "search": ""
				}""";

		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		// 发送 PUT 请求
		ResponseEntity<String> response = restTemplate.exchange(
				"https://www.modelscope.cn/openapi/v1/mcp/servers",
				HttpMethod.PUT,
				entity,
				String.class
		);

		// 处理响应
		if (response.getStatusCode().is2xxSuccessful()) {
			System.out.println("Response: " + response.getBody());
		} else {
			System.out.println("Request failed with status: " + response.getStatusCode());
		}
	}

}
