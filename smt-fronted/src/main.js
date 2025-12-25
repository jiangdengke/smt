import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import './style.css'
// Fonts
import 'vfonts/Lato.css'
import 'vfonts/FiraCode.css'

import App from './App.vue'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')