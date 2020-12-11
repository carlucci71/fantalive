package scrap;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

  
@Aspect
//@Component
public class LogAspect {
	@AfterThrowing(
			pointcut = "within(scrap.*)",
			throwing= "error")
	public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
      
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		String metodo = signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();
		String parametri = Arrays.asList(joinPoint.getArgs()).toString();
		System.out.println("Errore nel metodo : " + metodo + " - Parametri : " + parametri);
		error.printStackTrace(System.out);
 
	}
 
}
 