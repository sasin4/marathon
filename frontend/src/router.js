
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import RegistrationManager from "./components/RegistrationManager"

import PayManager from "./components/PayManager"

import RegisterMasterManager from "./components/RegisterMasterManager"


import Dashboard from "./components/Dashboard"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/registrations',
                name: 'RegistrationManager',
                component: RegistrationManager
            },

            {
                path: '/pays',
                name: 'PayManager',
                component: PayManager
            },

            {
                path: '/registerMasters',
                name: 'RegisterMasterManager',
                component: RegisterMasterManager
            },


            {
                path: '/dashboards',
                name: 'Dashboard',
                component: Dashboard
            },


    ]
})
