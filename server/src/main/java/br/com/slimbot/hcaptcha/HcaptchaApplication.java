package br.com.slimbot.hcaptcha;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import nu.pattern.OpenCV;

@SpringBootApplication
public class HcaptchaApplication {

	public static void main(String[] args) throws IOException {

		OpenCV.loadShared();

		new Thread(() -> startHSWNode()).start();

		SpringApplication.run(HcaptchaApplication.class, args);
	}

	private static void startHSWNode() {

		ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList("node", "index.js"));
		processBuilder.directory(new File("nodevm"));
		processBuilder.inheritIO();
		try {
			Process process = processBuilder.start();

			process.waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (IOException e) {

			System.err.println(e);
		}
	}

}
