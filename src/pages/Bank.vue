<template>
  <q-page class=" justify-evenly">
    <div class="q-pa-md">
      <h5 v-if="id === undefined">Edit existing bank {{ id }}</h5>
      <h5 v-else>Add new bank</h5>
      
      <div class="q-pa-md" style="max-width: 400px">
        <q-form
          @submit="onSubmit($props.id)"
          @reset="onCancel"
          class="q-gutter-md"
        >
          <q-input
            filled
            v-model="bank.name"
            label="Bank name *"
            hint="User defined name for this bank"
            lazy-rules
            :rules="[ val => val && val.length > 1 || 'Bank name must be at least 2 characters']"
          />

          <q-input
            filled
            v-model="bank.bankURL"
            label="EBICS URL"
            hint="EBICS bank URL, including https://"
            lazy-rules
            :rules="[
              val => val && val.length > 1 && validateUrl(val) || 'Please enter valid URL including http(s)://'
            ]"
          />

          <q-input
            filled
            v-model="bank.hostId"
            label="EBICS HOSTID"
            hint="EBICS HOST ID, example EBXUBSCH"
            lazy-rules
            :rules="[
              val => val && val.length > 0 || 'Please enter valid EBICS HOST ID, at least 1 character'
            ]"
          />

          <div>
            <q-btn v-if="id === undefined" label="Add" type="submit" color="primary"/>
            <q-btn v-else label="Update" type="submit" color="primary"/>
            <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" icon="undo" />
          </div>
        </q-form>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { api } from 'boot/axios'
import { Bank } from 'components/models';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'Banks',
  components: {  },
  props: {
    id: {
      type: Number,
      required: false,
      default: undefined
    }
  },
  data () {
    return {
      bank: 
        {
          bankURL: '',
          name: '',
          hostId: '',
        } as Bank
    }
  },
  methods: {
    validateUrl (url:string):boolean {
      const regex = /^(http(s)?:\/\/.)(www\.)?[-a-zA-Z0-9@:%._\+~#=]{0,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)$/;
      return regex.test(url)
    },
    loadData (bankId: number) {
      api.get<Bank>(`/banks/${bankId}`)
        .then((response) => {
          this.bank = response.data
        })
        .catch((error: Error) => {
          this.$q.notify({
            color: 'negative',
            position: 'bottom-right',
            message: `Loading failed: ${error.message}`,
            icon: 'report_problem'
          })
        })
    },
    onSubmit (bankId: number | undefined) {  
      if (bankId === undefined) {
        api.post<Bank>('/banks', this.bank)
          .then(() => {
            this.$q.notify({
              color: 'green-4',
              textColor: 'white',
              icon: 'cloud_done',
              message: 'Create done'
            })
            this.$router.go(-1);
          })
          .catch((error: Error) => {
            this.$q.notify({
              color: 'negative',
              position: 'bottom-right',
              message: `Creating failed: ${error.message}`,
              icon: 'report_problem'
            })
          })
      } else {
        api.put<Bank>(`/banks/${bankId}`, this.bank)
          .then(() => {
            this.$q.notify({
              color: 'green-4',
              textColor: 'white',
              icon: 'cloud_done',
              message: 'Update done'
            })
            this.$router.go(-1);
          })
          .catch((error: Error) => {
            this.$q.notify({
              color: 'negative',
              position: 'bottom-right',
              message: `Update failed: ${error.message}`,
              icon: 'report_problem'
            })
          })
      }
    },
    onCancel () {
      this.$router.go(-1);
    },
  },
  mounted() {
    if (this.$props.id !== undefined) {
      this.loadData(this.$props.id)
    }
  }
});
</script>
