<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <h5>User context</h5>
      <div class="q-pa-md" style="max-width: 400px">
        <div v-if="userContext">
          <q-list bordered padding>
            <q-item-label header>User Context</q-item-label>

            <q-item clickable v-ripple class="q-my-sm">              
              <q-item-section avatar>
                <q-avatar color="primary" text-color="white">
                  <img src="https://cdn.quasar.dev/img/boy-avatar.png">
                </q-avatar>
              </q-item-section>
              <q-item-section>
                <q-item-label caption>User Name</q-item-label>
                <q-item-label>
                  {{userContext.name}}
                </q-item-label>
              </q-item-section>
              <q-item-section side top>
                <q-btn
                  @click="onLogout"
                  label="Logout"
                  color="primary"
                  flat
                  class="q-ml-sm"
                  icon="undo"
                />
              </q-item-section>
            </q-item>

            <q-item clickable v-ripple>
              <q-item-section>
                <q-item-label caption>Logged at</q-item-label>
                <q-item-label>
                  {{userContext.time}}
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-separator spaced></q-separator>
            <q-item-label header>User roles</q-item-label>

            <q-item tag="label" v-ripple>
              <q-item-section side top>
                <q-checkbox v-model="roleGuest" disable></q-checkbox>
              </q-item-section>

              <q-item-section>
                <q-item-label>Guest</q-item-label>
                <q-item-label caption>
                  Guest can use predefined shared EBICS bank connections in order to upload / download files.
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-item tag="label" v-ripple>
              <q-item-section side top>
                <q-checkbox v-model="roleUser" disable></q-checkbox>
              </q-item-section>

              <q-item-section>
                <q-item-label>User</q-item-label>
                <q-item-label caption>
                  As user you can create your own bank connection for predefined banks, and use them in order to upload/download files. You can share such connections to other users as well.
                </q-item-label>
              </q-item-section>
            </q-item>

            <q-item tag="label" v-ripple>
              <q-item-section side top>
                <q-checkbox v-model="roleAdmin" disable></q-checkbox>
              </q-item-section>

              <q-item-section>
                <q-item-label>Admin</q-item-label>
                <q-item-label caption>
                  As administrator you can maintain list of bank available to users, maintain all user bank connections. 
                </q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </div>
        <div v-else>No user context available, try to log in</div>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import useUserContextAPI from 'components/usercontext';

export default defineComponent({
  name: 'UserContext',
  methods: {
    async onLogout() {
      await this.resetUserContextData()
    },
  },
  computed: {
    roleGuest(): boolean { return this.hasRole('GUEST'); },
    roleUser(): boolean { return this.hasRole('USER'); },
    roleAdmin(): boolean { return this.hasRole('ADMIN'); },
  },
  setup() {
    const { basicCredentials, userContext, hasRole, resetUserContextData, refreshUserContextData } = useUserContextAPI();
    return { basicCredentials, userContext, hasRole, resetUserContextData, refreshUserContextData };
  },
});
</script>
