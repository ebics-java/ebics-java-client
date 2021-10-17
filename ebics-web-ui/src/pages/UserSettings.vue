<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <div class="q-pa-md" style="max-width: 400px">
        <user-preferences saveButton/>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import UserPreferences from 'components/UserPreferences.vue'

export default defineComponent({
  name: 'UserContext',
  components: { UserPreferences },
  setup() {
    //const { userContext } = useUserContextAPI();
    return { };
  },
});
</script>
