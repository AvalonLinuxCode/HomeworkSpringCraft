package com.eduplatform.dto;

/**
 * 讲师新增/修改入参。
 *
 * @implNote 不加 Bean Validation 注解：校验逻辑集中在 Service 层 apply()，
 *           与 update() 共用同一份校验代码，避免 DTO 与 Service 校验不一致。
 *           若未来引入 Controller 层 @Valid，可在此处添加注解并删除 apply() 中的重复校验。
 */
public record InstructorDTO(String name, String avatar, String title, String description) {
}
