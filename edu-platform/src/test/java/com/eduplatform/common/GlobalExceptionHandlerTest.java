package com.eduplatform.common;

import com.eduplatform.dto.CourseQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ring A：{@link GlobalExceptionHandler} 的分支语义。不启动 Spring、不连数据库。
 *
 * <p>这里直接喂真实的包装异常类型，把「绑定路径怎么包装」与 advice 的判定解耦：
 * 即便将来 Spring 改了包装策略，这两条分支的契约仍然被钉住。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unwrapsValidationExceptionThrownByQueryConstructor() {
        BeanInstantiationException failure = new BeanInstantiationException(
                CourseQuery.class, "Failed to instantiate [com.eduplatform.dto.CourseQuery]",
                new ValidationException("课程状态只能是 draft / published / archived"));

        ResponseEntity<Result<Void>> response = handler.handleQueryInstantiation(failure);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("课程状态只能是 draft / published / archived");
        assertThat(response.getBody().code()).isEqualTo(400);
    }

    /**
     * 构造器里抛出的非应用异常是服务端缺陷：必须仍是 500，不能被降级成 400 掩盖掉。
     */
    @Test
    void internalFailureInsideQueryConstructorStaysHttp500() {
        BeanInstantiationException failure = new BeanInstantiationException(
                CourseQuery.class, "Failed to instantiate [com.eduplatform.dto.CourseQuery]",
                new NullPointerException("服务端自己的缺陷"));

        ResponseEntity<Result<Void>> response = handler.handleQueryInstantiation(failure);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("服务器内部错误");
    }

    @Test
    void applicationSubclassesKeepTheirOwnStatus() {
        assertThat(handler.handleApplication(new NotFoundException("课程不存在")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleApplication(new BusinessException("该分类下存在子分类")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * 未映射路径不该伪装成服务端故障：Spring 6 的默认资源兜底抛 NoResourceFoundException，
     * 以前会一路掉进兜底分支变成 500。
     */
    @Test
    void unmappedEndpointIs404Not500() {
        ResponseEntity<Result<Void>> response = handler.handleNoEndpoint(
                new NoResourceFoundException(HttpMethod.GET, "/api/admin/courses/unknown"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("接口不存在");
    }
}
