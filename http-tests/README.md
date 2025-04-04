# MuYun API 测试文件使用说明

这些HTTP测试文件用于测试MuYun平台的各个API接口。通过模块化的组织方式，可以更清晰地管理和执行测试。

## 文件结构

- `http-client.env.json`：环境变量配置
- `all-tests.http`：测试集合入口，包含测试执行顺序
- `security.http`：安全认证相关API测试（登录、Token验证等）
- `user.http`：用户管理相关API测试
- `infor-flow.http`：信息流相关API测试
- `comment.http`：评论相关API测试
- `feedback-report.http`：反馈和举报相关API测试
- `third-party.http`：第三方服务API测试

## 使用方法

### 准备工作

1. 安装HTTP客户端插件：
   - IntelliJ IDEA：RestClient插件（内置）
   - VSCode：Rest Client插件

2. 配置环境变量：
   - 打开`http-client.env.json`文件
   - 修改`baseUrl`为你的服务器地址

### 执行测试

**方法一：按顺序执行所有测试**

1. 打开`all-tests.http`文件
2. 选择右上角的环境选择器，选择"dev"环境
3. 通过"▶"按钮依次执行请求

**方法二：执行单独模块的测试**

1. 打开对应的模块测试文件（如`security.http`）
2. 选择环境
3. 执行需要的测试请求

### 测试流程说明

测试应当按以下顺序执行：

1. 用户创建（user.http）
2. 登录获取Token（security.http）
3. 创建信息流内容（infor-flow.http）
4. 创建评论（comment.http）
5. 其他测试

## Token处理

系统会自动处理Token：

1. 登录后，Token会被保存在环境变量中
2. 后续请求会自动使用保存的Token
3. 可通过刷新Token接口更新Token

## 注意事项

- 测试请求间存在依赖关系，请按照建议的顺序执行
- 部分请求使用了动态生成的ID，确保先执行前置请求
- 如果遇到认证问题，可能是Token过期，请重新登录 