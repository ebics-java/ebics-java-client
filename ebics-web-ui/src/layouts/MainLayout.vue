<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />
        <q-breadcrumbs active-color="white" style="font-size: 16px">
          <q-breadcrumbs-el label="Ebics Web Client" />
          <q-breadcrumbs-el
            v-for="menuItem in menuItemOfCurrentRoute"
            :key="menuItem.label"
            :label="menuItem.label"
          />
        </q-breadcrumbs>
        <q-space />
        <div v-if="userContext" class="text-caption on-left">
          version {{ userContext.appVersion }}
        </div>
        <q-btn v-if="userContext" unelevated to="/userctx"
          >User: '{{ userContext.name }}'</q-btn
        >
        <q-btn v-else unelevated to="/login">Login</q-btn>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered class="bg-grey-1">
      <q-scroll-area
        style="
          height: calc(100% - 0px);
          margin-top: 0px;
          border-right: 1px solid #ddd;
        "
      >
        <q-list padding>
          <q-item to="/banks" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="account_balance" />
            </q-item-section>

            <q-item-section> Banks </q-item-section>
          </q-item>

          <q-item to="/bankconnections" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="electrical_services" />
            </q-item-section>

            <q-item-section> Bank connections </q-item-section>
          </q-item>

          <q-item to="/upload/type=simple" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="list" />
            </q-item-section>

            <q-item-section> Simple file upload </q-item-section>
          </q-item>

          <q-item to="/upload/type=edit" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="edit" />
            </q-item-section>

            <q-item-section> Edit &amp; Upload file </q-item-section>
          </q-item>

          <q-item to="/download" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="file_download" />
            </q-item-section>

            <q-item-section> Download file </q-item-section>
          </q-item>

          <q-item to="/settings" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="settings" />
            </q-item-section>

            <q-item-section> Settings </q-item-section>
          </q-item>

          <q-item v-if="hasRoleAdmin" to="/traces" exact clickable v-ripple>
            <q-item-section avatar>
              <q-icon name="receipt_long" />
            </q-item-section>

            <q-item-section> Traces </q-item-section>
          </q-item>
        </q-list>
      </q-scroll-area>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import useUserContextAPI from 'src/components/user-context';
import { useRouter } from 'vue-router';
import { MenuItemRouteMeta } from 'src/components/vue-ext';

export default defineComponent({
  name: 'MainLayout',

  setup() {
    const leftDrawerOpen = ref(false);
    const { userContext, hasRoleAdmin } = useUserContextAPI();
    const router = useRouter();

    const menuItemOfCurrentRoute = computed((): MenuItemRouteMeta[] => {
      return router.currentRoute.value.matched
        .filter((rec) => rec.meta.label)
        .map((rec) => rec.meta as MenuItemRouteMeta);
    });

    return {
      menuItemOfCurrentRoute,
      userContext,
      hasRoleAdmin,
      leftDrawerOpen,
      toggleLeftDrawer() {
        leftDrawerOpen.value = !leftDrawerOpen.value;
      },
    };
  },
});
</script>
