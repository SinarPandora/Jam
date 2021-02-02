import Router from "vue-router";
import Vue from "vue";
import VueRouterBackButton from "vue-router-back-button";

Vue.use(Router);

export const router = new Router({
    routes: [
        {
            path: "/sxdl_editor",
            name: "sxdl_editor",
            component: () => import('@/components/editor/Editor')
        }
    ]
});

Vue.use(VueRouterBackButton, {router});
