<template>
    <div class="q-pa-md">
      <h5>User Login</h5>
      <div class="q-pa-md" style="max-width: 400px">
        <q-form @submit="onLogin" class="q-gutter-md">
          <q-input
            filled
            v-model="basicCredentials.username"
            label="User name"
            hint="User name used for login with HTTP simple authorization"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 0) ||
                'User name must be at least 1 characters',
            ]"
          />

          <q-input
            filled
            v-model="basicCredentials.password"
            type="password"
            label="User password"
            hint="User password used for login with HTTP simple authorization"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 1) ||
                'User password must be at least 2 characters',
            ]"
          />

          <div>
            <q-btn label="Login" type="submit" color="primary" />
          </div>
        </q-form>
      </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import useUserContextAPI from 'src/components/user-context';

export default defineComponent({
  name: 'UserLogin',
  methods: {
    async onLogin() {
      try {
        await this.refreshUserContextData()
        await this.$router.push({path: '/'})
      } catch(error) {
        console.log(JSON.stringify(error))
      }
    },
  },
  setup() {
    const { basicCredentials, refreshUserContextData } = useUserContextAPI();
    return { basicCredentials, refreshUserContextData };
  },
});
</script>
