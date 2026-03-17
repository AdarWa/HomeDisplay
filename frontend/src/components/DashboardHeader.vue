<template>
  <header class="dashboard-header">
    <div>
      <p class="eyebrow">HomeDisplay</p>
      <h1>Device Fleet Dashboard</h1>
      <p class="subtitle">Track every HomeDisplay node and jump into management actions.</p>
    </div>
  </header>
</template>

<style scoped>
.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
}

.dashboard-header h1 {
  font-size: 2.25rem;
  margin: 0.4rem 0 0.5rem;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-weight: 600;
  color: #60a5fa;
  margin: 0;
}

.subtitle {
  margin: 0;
  color: #9ca3af;
}

@media (max-width: 1024px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
