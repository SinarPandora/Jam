import Vue from 'vue';
import App from './App.vue';
import animated from 'animate.css/animate.compat.css';
import VueRx from "vue-rx";
import './registerServiceWorker';
import {http} from './plugins/http';
import store from './plugins/store';
import {router} from './plugins/router';
import vuetify from './plugins/vuetify';

Vue.config.productionTip = false;

Vue.use(VueRx);
Vue.use(animated);

new Vue({
    http,
    store,
    router,
    vuetify,
    render: h => h(App)
}).$mount('#app');
