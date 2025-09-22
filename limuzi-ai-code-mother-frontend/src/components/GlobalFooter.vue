<template>
  <div class="global-footer">
    <div class="footer-content">
      <p class="copyright">
        © 2024 limuzi-ai原创项目. All rights reserved.
      </p>
      <div class="footer-links">
        <a href="#" class="footer-link">隐私政策</a>
        <a href="#" class="footer-link">服务条款</a>
        <a href="#" class="footer-link">联系我们</a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 底部组件逻辑
</script>

<style scoped>
.global-footer {
  background: #001529;
  color: #fff;
  padding: 8px 0;
  text-align: center;
  position: relative;
  z-index: 1000;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.copyright {
  margin: 0 0 4px 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.footer-link {
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  font-size: 10px;
  transition: color 0.3s;
}

.footer-link:hover {
  color: #1890ff;
  text-decoration: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    padding: 0 16px;
  }

  .footer-links {
    gap: 12px;
  }

  .footer-link {
    font-size: 9px;
  }
}

@media (max-width: 480px) {
  .footer-content {
    padding: 0 12px;
  }

  .footer-links {
    gap: 8px;
  }

  .copyright {
    font-size: 10px;
  }
}
</style>
