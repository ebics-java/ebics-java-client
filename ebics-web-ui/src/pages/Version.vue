<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <h5>About</h5>
      <div class="q-pa-md" style="max-width: 400px">
        <div v-if="userContext">
          <q-list bordered padding>
            <q-item-label header>About Application</q-item-label>

            <q-item clickable v-ripple class="q-my-sm">              
              <q-item-section avatar>
                <q-avatar color="primary" text-color="white">
                  <q-icon name="verified_user" />
                </q-avatar>
              </q-item-section>
              <q-item-section>
                <q-item-label caption>App Name</q-item-label>
                <q-item-label>
                  EBICS Web Client
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-item clickable v-ripple>
              <q-item-section>
                <q-item-label caption>App Version</q-item-label>
                <q-item-label>
                  {{userContext.appVersion}}
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-item clickable v-ripple>
              <q-item-section>
                <q-item-label caption>App Build Timestamp</q-item-label>
                <q-item-label>
                  {{userContext.appBuildTimestamp}}
                </q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
          <user-preferences />
        </div>
        <div v-else>
          <p>No user context available, try to log in</p>
          <q-btn color="primary" to="/login">Login</q-btn>
        </div>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import useUserContextAPI from 'src/components/user-context';

export default defineComponent({
  name: 'UserContext',
  setup() {
    const { userContext } = useUserContextAPI();
    return { userContext };
  },
});
</script>
