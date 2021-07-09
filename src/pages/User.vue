<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <h5 v-if="id !== undefined">Edit existing user {{ id }}</h5>
      <h5 v-else>Add new user</h5>

      <div class="q-pa-md" style="max-width: 400px">
        <q-form
          @submit="onSubmit($props.id)"
          @reset="onCancel"
          class="q-gutter-md"
        >
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

          <q-input
            filled
            v-model="user.dn"
            label="User DN"
            hint="User domain name for certificat, example: cn=name,c=de,o=Organization,e=myemail@at.com"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 1) ||
                'Please enter valid DN at least 2 characters',
            ]"
          />

          <!-- use-input, fill-input, input-debounce="0" @filter="filterBank" hide-selected  -->
          <q-select
            filled
            v-model="user.partner.bank"
            :options="banks"
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
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 0) ||
                'Please enter valid EBICS Customer ID, at least 1 character',
            ]"
          />

          <div class="q-gutter-sm">
            <q-radio
              v-model="user.ebicsVersion"
              val="H003"
              contextmenu="test"
              label="EBICS 2.4 (H003)"
            />
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
          </div>

          <q-input filled v-model="user.userStatus" label="EBICS user status" />

          <q-stepper v-model="step" ref="stepper" color="primary" animated vertical>
            <q-step
              :name="1"
              title="Send user keys to bank"
              icon="forward_to_inbox"
              :done="step > 1"
            >
              Continue in order to create user keys and send them to the bank using the entered
              EBICS parameters (bank url, user, customer). For sending of the keys INI and HIA 
              administrative ordertypes will be used.
            </q-step>

            <q-step
              :name="2"
              title="Signing of user letters"
              caption="Optional"
              icon="email"
              :done="step > 2"
            >
              In order to activate the this EBICS user you have to provide bellow generated hash keys to your bank.
              The bank will check provided hash keys and activate the EBICS user. 
              Letter A005: XB CX 56 Letter E002: XB CX 56
                <q-btn
                  label="Print Letters"
                  color="primary"
                  class="q-ml-sm"
                  icon="print"
                ></q-btn>
            </q-step>

            <q-step :name="3" title="Download bank keys" icon="download" :done="step > 3">
              Continue in order to download bank keys from your bank.
            </q-step>

            <q-step :name="4" title="Verify bank keys" icon="gpp_good">
              Verify bellow downloaded bank keys with the one provided by your bank during onboarding. 
              In case they not match, this connection can't be trussted - identity of the bank is not valid.
            </q-step>

            <template v-slot:navigation>
              <q-stepper-navigation>
                <q-btn
                  v-if="step !== 4"
                  @click="$refs.stepper.next()"
                  color="primary"
                  label="Continue"
                ></q-btn>
                <q-btn
                  v-if="step > 1"
                  flat
                  color="primary"
                  @click="$refs.stepper.previous()"
                  label="Back"
                  class="q-ml-sm"
                ></q-btn>
              </q-stepper-navigation>
            </template>
          </q-stepper>

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
import { api } from 'boot/axios';
import { User, UserInfo, Partner, Bank } from 'components/models';
import { defineComponent } from 'vue';

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
  data() {
    return {
      step: 0,
      banks: [] as Bank[],
      user: {
        name: '',
        userId: '',
        partner: {
          partnerId: '',
          bank: {
            id: 0,
            name: '',
          } as Bank,
        } as Partner,
        ebicsVersion: 'H005',
        userStatus: 'CREATED',
      } as User,
    };
  },
  methods: {
    /**
     * Route to Bank page
     * bankId
     *  - if given then will be routed with 'id' parameter to edit page
     *  - if undefined will be routed without 'id' parameter to create page
     */
    async routeToCreateBankPage() {
      await this.$router.push({
        name: 'bank/create',
        params: undefined,
        query: { action: 'create' },
      });
    },
    validateUrl(url: string): boolean {
      const regex =
        /^(http(s)?:\/\/.)(www\.)?[-a-zA-Z0-9@:%._\+~#=]{0,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)$/;
      return regex.test(url);
    },
    loadBanksData() {
      api
        .get<Bank[]>('/banks')
        .then((response) => {
          this.banks = response.data;
        })
        .catch((error: Error) => {
          this.$q.notify({
            color: 'negative',
            position: 'bottom-right',
            message: `Loading failed: ${error.message}`,
            icon: 'report_problem',
          });
        });
    },
    loadUserData(userId: number) {
      api
        .get<User>(`/users/${userId}`)
        .then((response) => {
          this.user = response.data;
        })
        .catch((error: Error) => {
          this.$q.notify({
            color: 'negative',
            position: 'bottom-right',
            message: `Loading failed: ${error.message}`,
            icon: 'report_problem',
          });
        });
    },
    onSubmit(bankId: number | undefined) {
      if (bankId === undefined) {
        api
          .post<UserInfo>(
            `/users?ebicsPartnerId=${this.user.partner.partnerId}&bankId=${this.user.partner.bank.id}`,
            this.user as UserInfo
          )
          .then(() => {
            this.$q.notify({
              color: 'green-4',
              textColor: 'white',
              icon: 'cloud_done',
              message: 'Create done',
            });
            this.$router.go(-1);
          })
          .catch((error: Error) => {
            this.$q.notify({
              color: 'negative',
              position: 'bottom-right',
              message: `Creating failed: ${error.message}`,
              icon: 'report_problem',
            });
          });
      } else {
        api
          .put<Bank>(`/users/${bankId}`, this.user)
          .then(() => {
            this.$q.notify({
              color: 'green-4',
              textColor: 'white',
              icon: 'cloud_done',
              message: 'Update done',
            });
            this.$router.go(-1);
          })
          .catch((error: Error) => {
            this.$q.notify({
              color: 'negative',
              position: 'bottom-right',
              message: `Update failed: ${error.message}`,
              icon: 'report_problem',
            });
          });
      }
    },
    onCancel() {
      this.$router.go(-1);
    },
  },
  mounted() {
    if (this.$props.id !== undefined) {
      this.loadUserData(this.$props.id);
    }
    this.loadBanksData();
  },
});
</script>
