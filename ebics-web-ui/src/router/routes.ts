import { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/UserContext.vue') },
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
      { path: '/bankconnections', component: () => import('pages/BankConnections.vue') },
      {
        path: '/bankconnection',
        name: 'bankconnection/create',
        component: () => import('src/pages/BankConnection.vue'),
      },
      {
        path: '/bankconnection/:id',
        name: 'bankconnection/edit',
        component: () => import('pages/BankConnection.vue'),
        props: (route) => {
          const id = Number.parseInt(route.params.id as string, 10);
          if (Number.isNaN(id)) {
            return undefined;
          }
          return { id };
        },
      },
      {
        path: '/bankconnection/:id/init',
        name: 'bankconnection/init',
        component: () => import('pages/BankConnectionInitWizz.vue'),
        props: (route) => {
          const id = Number.parseInt(route.params.id as string, 10);
          if (Number.isNaN(id)) {
            return undefined;
          }
          return { id };
        },
      },
      { path: '/upload/type=:type', name: 'upload', component: () => import('pages/FileUpload.vue'),
        props: (route) => {
          const fileEditor = route.params.type == 'edit'
          return { fileEditor };
        },
      },
      { path: '/download', component: () => import('pages/FileDownload.vue') },
      { path: '/userctx', component: () => import('pages/UserContext.vue') },
      { path: '/version', component: () => import('pages/Version.vue') },
      { path: '/settings', component: () => import('pages/UserSettings.vue') },
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
