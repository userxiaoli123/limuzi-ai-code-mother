<script setup lang="ts">
import {ref} from 'vue'
import {Card, Row, Col, Button, Space} from 'ant-design-vue'

const features = ref([
  {
    title: '响应式设计',
    description: '支持桌面端和移动端，自适应不同屏幕尺寸',
    icon: 'MobileOutlined'
  },
  {
    title: '现代化UI',
    description: '基于 Ant Design Vue 组件库，界面美观易用',
    icon: 'DesignOutlined'
  },
  {
    title: '模块化架构',
    description: '组件化开发，代码结构清晰，易于维护',
    icon: 'AppstoreOutlined'
  }
])
</script>

<template>
  <div class="home-page">
    <div class="hero-section">
      <h1 class="hero-title">欢迎使用 Limuzi AI 项目</h1>
      <p class="hero-description">
        这是一个基于 Vue 3 + TypeScript + Ant Design Vue 的现代化前端项目
      </p>
      <Space size="large">
        <Button type="primary" size="large">开始使用</Button>
        <Button size="large">了解更多</Button>
      </Space>
    </div>

    <div class="features-section">
      <h2 class="section-title">核心特性</h2>
      <Row :gutter="[24, 24]">
        <Col :xs="24" :sm="12" :md="8" v-for="feature in features" :key="feature.title">
          <Card class="feature-card" hoverable>
            <template #title>
              <a-icon :type="feature.icon" class="feature-icon"/>
              {{ feature.title }}
            </template>
            <p>{{ feature.description }}</p>
          </Card>
        </Col>
      </Row>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.hero-section {
  text-align: center;
  padding: 60px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  margin-bottom: 60px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 24px;
  background: linear-gradient(45deg, #fff, #f0f0f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-description {
  font-size: 20px;
  margin-bottom: 32px;
  opacity: 0.9;
}

.features-section {
  margin-bottom: 60px;
}

.section-title {
  text-align: center;
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 48px;
  color: #333;
}

.feature-card {
  height: 100%;
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-icon {
  font-size: 24px;
  color: #1890ff;
  margin-right: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }

  .hero-description {
    font-size: 18px;
  }

  .hero-section {
    padding: 40px 20px;
  }

  .home-page {
    padding: 0 16px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-description {
    font-size: 16px;
  }

  .section-title {
    font-size: 24px;
  }
}
</style>
