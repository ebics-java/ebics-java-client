package org.ebics.client.ebicsrestapi

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import javax.servlet.http.HttpServletRequest

@Aspect
@Component
class LoggingAspect {

    @Pointcut("execution(* org.ebics.client.ebicsrestapi..*(..))")
    private fun getLoggedMethods() {}

    @Around("getLoggedMethods()")
    @Throws(Throwable::class)
    fun profileAllMethods(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = proceedingJoinPoint.signature as MethodSignature

        val className = methodSignature.declaringType.simpleName
        val methodName = methodSignature.name
        val stopWatch = StopWatch()

        LOGGER.info("Entering $className.$methodName" )

        //Measure method execution time
        stopWatch.start()
        val result = proceedingJoinPoint.proceed()
        stopWatch.stop()

        //Log method execution time
        LOGGER.info("Exiting  $className.$methodName  time: ${stopWatch.totalTimeMillis} ms")
        return result
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(
            LoggingAspect::class.java
        )
    }
}
