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

        <q-toolbar-title>
          EBICS Web Client
        </q-toolbar-title>
        <q-btn v-if="userContext" unelevated to="/userctx">User: '{{userContext.name}}'</q-btn>
        <q-btn v-else unelevated to="/login">Login</q-btn>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      bordered
      class="bg-grey-1"
    >
      <q-scroll-area style="height: calc(100% - 0px); margin-top: 0px; border-right: 1px solid #ddd">
        <q-list padding>
          <q-item 
            to="/banks"
            exact
            clickable 
            v-ripple>
            <q-item-section avatar>
              <q-icon name="account_balance" />
            </q-item-section>

            <q-item-section>
              Banks
            </q-item-section>
          </q-item>

          <q-item 
            to="/bankconnections"
            exact
            clickable 
            v-ripple>
            <q-item-section avatar>
              <q-icon name="electrical_services" />
            </q-item-section>

            <q-item-section>
              Bank connections
            </q-item-section>
          </q-item>

          <q-item 
            to="/upload/type=simple"
            exact
            clickable 
            v-ripple>
            <q-item-section avatar>
              <q-icon name="list" />
            </q-item-section>

            <q-item-section>
              Simple file upload
            </q-item-section>
          </q-item>

          <q-item 
            to="/upload/type=edit"
            exact
            clickable 
            v-ripple>
            <q-item-section avatar>
              <q-icon name="edit" />
            </q-item-section>

            <q-item-section>
              Edit &amp; Upload file
            </q-item-section>
          </q-item>

          <q-item 
            to="/help"
            exact
            clickable 
            v-ripple>
            <q-item-section avatar>
              <q-icon name="help" />
            </q-item-section>

            <q-item-section>
              Help
            </q-item-section>
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

import { defineComponent, ref } from 'vue'
import useUserContextAPI from 'src/components/user-context';

export default defineComponent({
  name: 'MainLayout',

  setup () {
    const leftDrawerOpen = ref(false)
    const { userContext } = useUserContextAPI();

    return {
      userContext,
      leftDrawerOpen,
      toggleLeftDrawer () {
        leftDrawerOpen.value = !leftDrawerOpen.value
      }
    }
  }
})
</script>
