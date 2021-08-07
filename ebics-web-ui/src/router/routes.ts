import { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/Users.vue') },
      { path: '/banks', component: () => import('pages/Banks.vue') },
      {
        path: '/bank',
        name: 'bank/create',
        component: () => import('pages/Bank.vue'),
      },
      {
        path: '/bank/:id',
        name: 'bank/edit',
        component: () => import('pages/Bank.vue'),
        props: (route) => {
          const id = Number.parseInt(route.params.id as string, 10);
          if (Number.isNaN(id)) {
            return undefined;
          }
          return { id };
        },
      },
      { path: '/users', component: () => import('pages/Users.vue') },
      {
        path: '/user',
        name: 'user/create',
        component: () => import('pages/User.vue'),
      },
      {
        path: '/user/:id',
        name: 'user/edit',
        component: () => import('pages/User.vue'),
        props: (route) => {
          const id = Number.parseInt(route.params.id as string, 10);
          if (Number.isNaN(id)) {
            return undefined;
          }
          return { id };
        },
      },
      { path: '/upload', component: () => import('pages/FileUpload.vue') },
      { path: '/userctx', component: () => import('pages/UserContext.vue') },
      { path: '/userlogin', component: () => import('pages/UserLogin.vue') },
    ],
  },

  {
    path: '/login',
    component: () => import('pages/UserLogin.vue'),
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/Error404.vue'),
  },
];

export default routes;
