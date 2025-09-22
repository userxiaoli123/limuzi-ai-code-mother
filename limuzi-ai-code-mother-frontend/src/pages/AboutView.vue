<template>
  <div class="about-page">
    <div class="about-content">
      <h1 class="page-title">关于我们</h1>
      <div class="about-description">
        <p>
          Limuzi AI 是一个专注于人工智能技术的前端项目，我们致力于为用户提供现代化的界面体验。
        </p>
        <p>
          本项目采用 Vue 3 + TypeScript + Ant Design Vue 技术栈，具有响应式设计、组件化架构等特点。
        </p>
      </div>

      <div class="tech-stack">
        <h2>技术栈</h2>
        <div class="tech-list">
          <div class="tech-item">Vue 3</div>
          <div class="tech-item">TypeScript</div>
          <div class="tech-item">Ant Design Vue</div>
          <div class="tech-item">Vue Router</div>
          <div class="tech-item">Pinia</div>
          <div class="tech-item">Vite</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;
}

.page-title {
  font-size: 36px;
  font-weight: 600;
  color: #333;
  margin-bottom: 32px;
  text-align: center;
}

.about-description {
  margin-bottom: 48px;
  line-height: 1.8;
}

.about-description p {
  font-size: 16px;
  color: #666;
  margin-bottom: 16px;
}

.tech-stack h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  text-align: center;
}

.tech-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.tech-item {
  background: #f0f2f5;
  color: #1890ff;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.tech-item:hover {
  background: #1890ff;
  color: white;
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-page {
    padding: 20px 16px;
  }

  .page-title {
    font-size: 28px;
  }

  .tech-stack h2 {
    font-size: 20px;
  }

  .tech-item {
    font-size: 12px;
    padding: 6px 12px;
  }
}
</style>
