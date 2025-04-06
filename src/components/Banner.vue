<template>
  <div class="banner">
    <div class="logo-container">
      <div class="competition-logo">
        <img src="../assets/competition-logo.png" alt="服务外包大赛logo" />
      </div>
      <div class="team-logo">
        <img src="../assets/team-logo.png" alt="队伍logo" />
      </div>
    </div>
    <div class="title-container">
      <h1 class="main-title">服务外包大赛 - 浓烟环境人体目标判别 - 项目演示</h1>
      <h2 class="team-name">It's mAI go!!!!!</h2>
    </div>
  </div>
</template>

<script setup lang="ts" />

<style scoped>
.banner {
  width: 100%;
  background: linear-gradient(135deg, #0284b9 0%, #015f86 100%);
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-bottom: 2px solid #03a9f4;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.competition-logo img,
.team-logo img {
  height: 80px;
  object-fit: contain;
  border-radius: 8px;
}

.title-container {
  text-align: center;
  flex-grow: 1;
}

.main-title {
  color: #ffffff;
  font-size: 30px;
  margin: 0;
  letter-spacing: 1px;
  font-weight: bold;
}

.team-name {
  color: #9dd1f0;
  font-size: 24px;
  margin: 5px 0 0;
  font-style: italic;
}

@media (max-width: 768px) {
  .banner {
    flex-direction: column;
    padding: 12px;
  }

  .main-title {
    font-size: 22px;
  }

  .team-name {
    font-size: 16px;
  }
}
</style>