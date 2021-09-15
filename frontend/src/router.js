
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import ApplyManager from "./components/ApplyManager"

import PayManager from "./components/PayManager"

import ApplyManagerManager from "./components/ApplyManagerManager"


import Dashboard from "./components/Dashboard"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/applies',
                name: 'ApplyManager',
                component: ApplyManager
            },

            {
                path: '/pays',
                name: 'PayManager',
                component: PayManager
            },

            {
                path: '/applyManagers',
                name: 'ApplyManagerManager',
                component: ApplyManagerManager
            },


            {
                path: '/dashboards',
                name: 'Dashboard',
                component: Dashboard
            },


    ]
})
