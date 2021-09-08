<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <h5 v-if="id !== undefined">
        Edit existing bank connection {{ user.name }}
      </h5>
      <h5 v-else>Add new bank connection</h5>

      <div class="q-pa-md" style="max-width: 400px">
        <q-form @submit="onSubmit()" @reset="onCancel" class="q-gutter-md">
          <q-input
            filled
            v-model="user.name"
            label="User name"
            hint="Customer user name (used for displaying)"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 1) ||
                'User name must be at least 2 characters',
            ]"
          />

          <!-- use-input, fill-input, input-debounce="0" @filter="filterBank" hide-selected  -->
          <q-select
            filled
            v-model="user.partner.bank"
            :options="banks"
            :disable="userStatusInitializing"
            option-label="name"
            hint="EBICS Bank"
            lazy-rules
            :rules="[(val) => val.id != 0 || 'Please select valid EBICS Bank']"
          />

          <q-input
            filled
            v-model="user.userId"
            label="EBICS User ID"
            hint="EBICS User ID, example CHT00034"
            :disable="userStatusInitializing"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 0) ||
                'Please enter valid EBICS User ID, at least 1 character',
            ]"
          />

          <q-input
            filled
            v-model="user.partner.partnerId"
            label="EBICS Partner ID"
            hint="EBICS Partner ID, example CH100208"
            :disable="userStatusInitializing"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 0) ||
                'Please enter valid EBICS Customer ID, at least 1 character',
            ]"
          />

          <q-field
            outlined
            label="Default EBICS Version used"
            stack-label
            hint="Can be changed or set any time later manually"
          >
            <template v-slot:control>
              <!-- q-radio
                  v-model="user.ebicsVersion"
                  :disable="userStatusInitializing"
                  val="H003"
                  contextmenu="test"
                  label="EBICS 2.4 (H003)"
                /-->
              <q-radio
                v-model="user.ebicsVersion"
                val="H004"
                label="EBICS 2.5 (H004)"
              />
              <q-radio
                v-model="user.ebicsVersion"
                val="H005"
                label="EBICS 3.0 (H005)"
              />
            </template>
          </q-field>

          <q-checkbox
            v-model="user.guestAccess"
            label="Share this bank connection"
            hint="If enabled then this connection will be available to every GUEST user, used mainly for testing"
          />

          <q-input
            disable
            filled
            v-model="user.dn"
            label="User DN"
            hint="Certificate user domain name"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 1) ||
                'Please enter valid DN at least 2 characters',
            ]"
          />

          <q-checkbox
            v-if="id !== undefined"
            disable
            v-model="user.usePassword"
            label="Protect your private keys with password (2FA)"
          />

          <div>
            <q-btn
              v-if="id === undefined"
              label="Add"
              type="submit"
              color="primary"
            />
            <q-btn v-else label="Update" type="submit" color="primary" />
            <q-btn
              label="Cancel"
              type="reset"
              color="primary"
              flat
              class="q-ml-sm"
              icon="undo"
            />
          </div>
        </q-form>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import useUserDataAPI from 'components/bankconnection';
import useBanksDataAPI from 'src/components/banks';

export default defineComponent({
  name: 'User',
  components: {},
  props: {
    id: {
      type: Number,
      required: false,
      default: undefined,
    },
  },
  methods: {
    async onSubmit() {
      await this.createOrUpdateUserData();
    },
    onCancel() {
      this.$router.go(-1);
    },
  },
  setup(props) {
    const { user, createOrUpdateUserData } = useUserDataAPI(props.id);
    const { banks } = useBanksDataAPI();
    const userStatusInitializing = computed((): boolean => {
      return (
        user.value.userStatus != 'CREATED' && user.value.userStatus != 'NEW'
      );
    });
    return { banks, user, createOrUpdateUserData, userStatusInitializing };
  },
});
</script>
