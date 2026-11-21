# 模型图目录

> 存放在线教育平台 UML 模型图，使用 [draw.io](https://app.diagrams.net/) 绘制（开源免费）。

---

## 模型清单

| # | 模型名称 | 文件 | 用途 | 状态 |
|---|----------|------|------|------|
| 1 | 用例图 | `use_case.drawio` | 需求分析：功能模块与参与者关系 | ⬜ 待绘制 |
| 2 | 体系结构图 | `architecture.drawio` | 架构设计：分层与模块 | ⬜ 待绘制 |
| 3 | E-R 图 | `er_diagram.drawio` | 数据库设计：实体与关系 | ⬜ 待绘制 |
| 4 | 类图 | `class_diagram.drawio` | 详细设计：类与关系 | ⬜ 待生成 |
| 5 | 活动图 | `activity_diagram.drawio` | 业务流程：课程发布/购买流程 | ⬜ 待绘制 |
| 6 | 时序图 | `sequence_diagram.drawio` | 交互细节：讲师管理流程 | ⬜ 待绘制 |

---

## 绘制指南

### 用例图

- **参与者**：管理员、学员、系统
- **用例**：
  - 后台：登录、讲师管理、课程管理、分类管理
  - 前台：注册/登录、浏览课程、浏览讲师、购买课程

### E-R 图

- **实体**：user、instructor、category、course、order
- **关系**：
  - user 1:N order
  - instructor 1:N course
  - category 1:N category（自引用）
  - category 1:N course
  - course 1:N order

### 类图

- 使用 IDEA 插件自动生成
- 包：`com.eduplatform.*`
- 实体类 + Service 接口 + Controller

### 活动图

- 课程发布活动：草稿 → 上架 → 下架
- 课程购买活动：浏览 → 加入购物车 → 支付 → 完成

---

## 文件命名规范

```
{模型类型}_{模块}_{版本}.drawio

示例：
- use_case_overall_v1.0.drawio
- er_diagram_core_v1.0.drawio
- class_diagram_service_v1.0.drawio
```

---

## draw.io 使用提示

1. **在线版**：https://app.diagrams.net/ （无需登录，直接使用）
2. **桌面版**：下载 [draw.io Desktop](https://github.com/jgraph/drawio-desktop/releases)
3. **VS Code 插件**：安装 "Draw.io Integration" 插件，可在 IDE 内编辑
4. **UML 模板**：新建图表时选择 "Create New Diagrams" → "Software Design" → "UML"
5. **导出格式**：保存为 `.drawio` 格式（XML），可导出 PNG/SVG 用于文档

---

## UML 形状库

在 draw.io 中启用以下形状库：

- **UML**：用例、类、活动、时序、组件图元素
- **General**：矩形、椭圆、箭头、文本
- **Flowchart**：流程图符号（活动图用）

---

*生成时间：2026-11-19*
