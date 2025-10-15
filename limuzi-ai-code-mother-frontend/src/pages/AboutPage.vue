<template>
  <div class="about-page">
    <a-row :gutter="[24, 24]">
      <a-col :span="24">
        <a-card class="about-card">
          <h1 class="about-title">关于 Limuzi AI</h1>
          <a-divider />
          <div class="about-content">
            <div class="about-section">
              <h2>项目介绍</h2>
              <p>Limuzi AI 是一个智能化应用开发平台，旨在帮助开发者和企业快速构建、部署和管理 AI 驱动的应用程序。我们提供了丰富的工具和服务，使 AI 技术的应用变得简单高效。</p>
            </div>

            <div class="about-section">
              <h2>核心功能</h2>
              <a-row :gutter="[16, 16]">
                <a-col :xs="24" :sm="12" :md="8">
                  <a-card class="feature-card">
                    <template #cover>
                      <div class="feature-icon">
                        <code-outlined />
                      </div>
                    </template>
                    <a-card-meta title="智能代码生成">
                      <template #description>基于先进的 AI 模型，自动生成高质量代码，提高开发效率。</template>
                    </a-card-meta>
                  </a-card>
                </a-col>
                <a-col :xs="24" :sm="12" :md="8">
                  <a-card class="feature-card">
                    <template #cover>
                      <div class="feature-icon">
                        <api-outlined />
                      </div>
                    </template>
                    <a-card-meta title="应用管理">
                      <template #description>便捷管理您的所有 AI 应用，一键部署，实时监控。</template>
                    </a-card-meta>
                  </a-card>
                </a-col>
                <a-col :xs="24" :sm="12" :md="8">
                  <a-card class="feature-card">
                    <template #cover>
                      <div class="feature-icon">
                        <message-outlined />
                      </div>
                    </template>
                    <a-card-meta title="智能对话">
                      <template #description>与 AI 助手进行自然语言交流，获取即时帮助和建议。</template>
                    </a-card-meta>
                  </a-card>
                </a-col>
              </a-row>
            </div>

            <div class="about-section">
              <h2>技术栈</h2>
              <p>我们的平台采用现代化的技术栈构建：</p>
              <a-row :gutter="[16, 16]" class="tech-stack">
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="blue">Vue 3</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="green">TypeScript</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="purple">Ant Design Vue</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="orange">Vite</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="cyan">Spring Boot</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="magenta">LangChain4j</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="volcano">LangGraph4j</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="red">Redis</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="gold">COS对象存储</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="lime">Nginx</a-tag>
                </a-col>
                <a-col :xs="12" :sm="8" :md="6" :lg="4">
                  <a-tag color="geekblue">ARMS</a-tag>
                </a-col>
              </a-row>
            </div>

            <div class="about-section">
              <h2>联系我们</h2>
              <p>如果您有任何问题或建议，欢迎联系我们：</p>
              <p><github-outlined /> GitHub: <a href="https://github.com/userxiaoli123/limuzi-ai-code-mother" target="_blank">github.com/limuzi-ai</a></p>
            </div>

            <div class="about-section">
              <h2>版本信息</h2>
              <p>当前版本: v1.0.0</p>
              <p>© {{ currentYear }} Limuzi AI. 保留所有权利。</p>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import {
  CodeOutlined,
  ApiOutlined,
  MessageOutlined,
  MailOutlined,
  GithubOutlined
} from '@ant-design/icons-vue';

const currentYear = new Date().getFullYear();
</script>

<style scoped>
.about-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.about-card {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.about-title {
  font-size: 28px;
  font-weight: 600;
  color: #1677ff;
  text-align: center;
  margin-bottom: 16px;
}

.about-content {
  font-size: 16px;
  line-height: 1.6;
}

.about-section {
  margin-bottom: 32px;
}

.about-section h2 {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 16px;
  color: #333;
}

.feature-card {
  height: 100%;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
  font-size: 36px;
  color: #1677ff;
  background-color: #f0f5ff;
}

.tech-stack {
  margin-top: 16px;
}

.tech-stack .ant-tag {
  padding: 4px 8px;
  font-size: 14px;
}

a {
  color: #1677ff;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
</style>
