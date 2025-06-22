package team.budderz.buddyspace.global.aspect;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RequestLoggingAspect {
	private static final Logger logger = LoggerFactory.getLogger(RequestLoggingAspect.class);
	private static final String REQUEST_ID = "requestId";

	@Pointcut("within(team.budderz.buddyspace.api..controller..*)")
	public void controllerMethods() {}

	// @Around 어드바이스로 메서드 실행을 감싸서 처리
	@Around("controllerMethods()")
	public Object logRequestDetails(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		String requestId = UUID.randomUUID().toString().substring(0, 8);
		HttpServletRequest request = null;

		try {
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				request = attributes.getRequest();

				MDC.put(REQUEST_ID, "[" + requestId + "]");

				String requestURI = request.getRequestURI();
				String method = request.getMethod();

				logger.info("Request Start: {} {}", method, requestURI);
			} else {
				logger.warn("Request context not found for method: {}", proceedingJoinPoint.getSignature().toShortString());
			}

			long startTime = System.currentTimeMillis();
			Object result = proceedingJoinPoint.proceed();
			long endTime = System.currentTimeMillis();
			long executionTime = endTime - startTime;

			if (request != null) {
				logger.info("Request End: [{} {}] processed successfully. It took {}ms", request.getMethod(), request.getRequestURI(), executionTime);
			}

			return result;
		} catch (Throwable ex) {
			if (request != null) {
				logger.error("Request Error: [{} {}] failed with exception: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
			} else {
				logger.error("Request Error: failed with exception: {}", ex.getMessage(), ex);
			}
			throw ex;
		} finally {
			MDC.remove(REQUEST_ID);
		}
	}
}
