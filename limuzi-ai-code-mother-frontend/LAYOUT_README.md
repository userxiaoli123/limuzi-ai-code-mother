# 全局基础布局说明

## 项目结构

```
src/
├── layouts/
│   └── BasicLayout.vue          # 全局基础布局组件
├── components/
│   ├── GlobalHeader.vue         # 全局导航栏组件
│   └── GlobalFooter.vue         # 全局底部组件
├── views/
│   ├── HomeView.vue             # 首页
│   └── AboutView.vue            # 关于页面
├── App.vue                      # 应用入口
└── main.ts                      # 主入口文件
```

## 布局特性

### 1. 上中下布局结构

- **顶部导航栏**: 固定在页面顶部，包含 Logo、网站标题、菜单项和登录按钮
- **中间内容区**: 根据路由动态切换页面内容
- **底部版权**: 固定在页面底部，显示版权信息

### 2. 响应式设计

- 支持桌面端、平板和移动端
- 使用 CSS Media Queries 实现不同屏幕尺寸的适配
- 移动端优化菜单显示

### 3. 技术实现

- 基于 Ant Design Vue 的 Layout 组件
- 使用 Vue 3 Composition API
- TypeScript 类型支持
- 模块化组件设计

## 组件说明

### BasicLayout.vue

全局基础布局组件，负责整体页面结构：

- 使用 `a-layout` 实现上中下布局
- 固定导航栏和底部位置
- 内容区域自适应高度

### GlobalHeader.vue

全局导航栏组件：

- Logo 和网站标题展示
- 水平菜单导航
- 登录按钮（可扩展为用户信息）
- 响应式菜单设计

### GlobalFooter.vue

全局底部组件：

- 版权信息展示
- 底部链接
- 固定定位

## 使用方式

1. 在 `App.vue` 中引入 `BasicLayout` 组件
2. 通过路由配置切换不同页面
3. 各页面内容会自动显示在中间内容区域

## 样式特点

- 移除了默认的 `main.css` 样式文件
- 使用 Ant Design Vue 的默认样式
- 自定义全局样式重置
- 响应式断点设计

## 扩展建议

1. 可以在 `GlobalHeader` 中添加用户登录状态管理
2. 可以扩展菜单配置，支持多级菜单
3. 可以添加主题切换功能
4. 可以添加面包屑导航
