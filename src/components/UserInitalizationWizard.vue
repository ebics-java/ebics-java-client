<template>
  <q-stepper
    v-model="initialUserStep"
    color="primary"
    ref="wizz"
    animated
    vertical
  >
    <q-step
      :name="UserIniWizzStep.CreateUserKeys"
      title="Create user keys"
      icon="forward_to_inbox"
      :done="stepDone(UserIniWizzStep.CreateUserKeys)"
    >
      Continue in order to create user private public keys. You can optionally
      set PIN to protect access to your private keys.
      <q-checkbox
        v-model="user.usePassword"
        label="Protect your private keys with password (2FA)"
      />
      <q-stepper-navigation>
        <q-btn
          @click="createUserKeys()"
          color="primary"
          label="Continue"
        ></q-btn>
      </q-stepper-navigation>
    </q-step>

    <q-step
      :name="UserIniWizzStep.UploadUserKeys"
      title="Upload user keys to bank"
      icon="forward_to_inbox"
      :done="stepDone(UserIniWizzStep.UploadUserKeys)"
    >
      Continue in order to create user keys and send them to the bank using the
      entered EBICS parameters (bank url, user, customer). For sending of the
      keys INI and HIA administrative ordertypes will be used.
      <q-checkbox
        v-model="user.usePassword"
        label="Protect your private keys with password (2FA)"
      />
      <q-stepper-navigation>
        <q-btn
          @click="uploadUserKeys()"
          color="primary"
          label="Continue"
        ></q-btn>
      </q-stepper-navigation>
    </q-step>

    <q-step
      :name="UserIniWizzStep.PrintUserLetters"
      title="Signing of user letters"
      caption="Optional"
      icon="email"
      :done="stepDone(UserIniWizzStep.PrintUserLetters)"
    >
      In order to activate the this EBICS user you have to provide bellow
      generated hash keys to your bank. The bank will check provided hash keys
      and activate the EBICS user. Letter A005: XB CX 56 Letter E002: XB CX 56
      <q-btn
        label="Print Letters"
        color="primary"
        class="q-ml-sm"
        icon="print"
      ></q-btn>
      <q-stepper-navigation>
        <q-btn
          @click="printUserLetters()"
          color="primary"
          label="Continue"
        ></q-btn>
      </q-stepper-navigation>
    </q-step>

    <q-step
      :name="UserIniWizzStep.DownloadBankKeys"
      title="Download bank keys"
      icon="download"
      :done="stepDone(UserIniWizzStep.DownloadBankKeys)"
    >
      Continue in order to download bank keys from your bank.
      <q-stepper-navigation>
        <q-btn
          @click="downloadBankKeys()"
          color="primary"
          label="Continue"
        ></q-btn>
      </q-stepper-navigation>
    </q-step>

    <q-step
      :name="UserIniWizzStep.VerifyBankKeys"
      title="Verify bank keys"
      icon="gpp_good"
      :done="stepDone(UserIniWizzStep.VerifyBankKeys)"
    >
      Verify bellow downloaded bank keys with the one provided by your bank
      during onboarding. In case they not match, this connection can't be
      trussted - identity of the bank is not valid.
      <q-stepper-navigation>
        <q-btn @click="verifyBankKeys()" color="primary" label="Finish"></q-btn>
      </q-stepper-navigation>
    </q-step>
  </q-stepper>
</template>

<script lang="ts">
import { reactive, Ref, ref, defineComponent, PropType } from 'vue';
import { api } from 'boot/axios';
import {
  UserIniWizzStep,
  UserWizz,
  User,
  UserPassword,
} from 'components/models';
import { QStepper } from 'quasar';
import { useQuasar } from 'quasar';

export default defineComponent({
  name: 'UserInitalizationWizard',
  props: {
    modelValue: {
      type: Object as PropType<UserWizz>,
      required: true,
    },
  },
  emits: ['update:modelValue'],
  data() {
    return {
      //Copy of binded user object in order to allow modifications
      //user: this.modelValue,
      tempPassword: undefined as string | undefined,
      UserIniWizzStep,
      //This property is reflecting actual step of the wizard,
      //it can be set by any change of user status - resetCurrentStep,
      //and from the wizard clicking itself.
      currentUserStep: UserIniWizzStep.CreateUserKeys,
    };
  },
  watch: {
    //Watch changes on user object in order to emit them to parent
    user: {
      deep: true,
      handler(newUser: UserWizz) {
        //Synchronize the actual step
        //this.resetCurrentStep(this.userStatusToStep(newUser.userStatus));
        //this.$emit('update:modelValue', newUser);
      },
    },
  },
  computed: {
    /*
     * This property reflects step which is calculated from actual userStatus,
     * It is needed in order to know which steps are actually finished
     */
    initialUserStep(): UserIniWizzStep {
      return this.userStatusToStep(this.user.userStatus);
    },
    /*usePassword: {
      get():boolean {
        return this.modelValue.usePassword
      },
      set(value:boolean) {
        this.$emit('update:modelValue', value)
      }
    },*/
  },
  methods: {
    resetCurrentStep(step: UserIniWizzStep) {
      //TBD logic which set the current step only if needed
      //(distance between current and step is logically not possible)
      this.currentUserStep = step;
    },
    nextStep() {
      (this.$refs.wizz as QStepper).next();
    },
    //Is called whenever the status of EBICS server was changed
    // in order to actualize the actual step over initialUserStep
    refreshUserStatus() {
      api
        .get<User>(`/users/${this.user.id}`)
        .then((response) => {
          this.$emit('update:modelValue', response.data);
        })
        .catch((error: Error) => {
          this.$q.notify({
            color: 'negative',
            position: 'bottom-right',
            message: `Refresh user data failed: ${error.message}`,
            icon: 'report_problem',
          });
        });
    },
    /*
      Create EBICS User Keys
    */
    createUserKeys() {
      //Get password for user certificates
      this.promptCertPassword(true)
        .then((pass) => {
          //Upload user keys (INI and/or HIA) depending on user status
          console.log(`Pass: ${pass}`);
          return this.createUserKeysAPI({ password: pass }).catch(
            (error: Error) => {
              this.$q.notify({
                color: 'negative',
                position: 'bottom-right',
                message: error.message,
                icon: 'report_problem',
              });
            }
          );
        })
        .then(() => {
          //Refresshing user status on success
          this.refreshUserStatus();
        })
        .catch((error: string) => {
          console.log(error);
        });
    },
    createUserKeysAPI(pass: UserPassword) {
      return new Promise<void>((resolve, reject) => {
        api
          .post<UserPassword>(`/users/${this.user.id}/certificates`, pass)
          .then(() => {
            this.$q.notify({
              color: 'positive',
              position: 'bottom-right',
              message: `Certificates created successfully for user dn ${this.user.dn}`,
              icon: 'gpp_good',
            });
            resolve();
          })
          .catch((error: Error) => {
            reject(`Create certificates failed: ${error.message}`);
          });
      });
    },
    /**
     * Asking for user certificat password, if required
     * createPass=true in order to ask for new password
     * createPass=false in order to ask for existing password
     */
    promptCertPassword(createPass: boolean) {
      return new Promise<string>((resolve, reject) => {
        if (!this.user.usePassword) {
          console.log('No pass required');
          resolve(''); //No password required
        } else {
          //Password required, did we store some already?
          if (this.tempPassword !== undefined) {
            console.log(`Temp pass used ${this.tempPassword}`);
            resolve(this.tempPassword);
          } else {
            //We will ask user for password
            resolve(this.passwordDialog(createPass));
          }
        }
      });
    },
    uploadUserKeys() {
      //Get password for user certificates
      this.promptCertPassword(false)
        .then((pass) => {
          //Upload user keys (INI and/or HIA) depending on user status
          return this.uploadUserKeysINI({ password: pass })
            .then(() => {
              return this.uploadUserKeysHIA({ password: pass });
            })
            .catch((error: Error) => {
              this.$q.notify({
                color: 'negative',
                position: 'bottom-right',
                message: error.message,
                icon: 'report_problem',
              });
            });
        })
        .then(() => {
          //Refresshing user status on success
          this.refreshUserStatus();
        })
        .catch((error: string) => {
          console.log(error);
        });
    },
    uploadUserKeysINI(pass: UserPassword) {
      return new Promise<void>((resolve, reject) => {
        if (this.user.userStatus == 'PARTLY_INITIALIZED_INI') resolve();
        else {
          api
            .post<UserPassword>(
              `/users/${this.user.id}/${this.user.ebicsVersion}/sendINI`,
              pass
            )
            .then(() => {
              this.$q.notify({
                color: 'positive',
                position: 'bottom-right',
                message: `INI uploaded successfully for user dn ${this.user.dn}`,
                icon: 'gpp_good',
              });
              resolve();
            })
            .catch((error: Error) => {
              reject(new Error(`INI failed: ${error.message}`));
            });
        }
      });
    },
    uploadUserKeysHIA(pass: UserPassword) {
      return new Promise<void>((resolve, reject) => {
        if (this.user.userStatus == 'PARTLY_INITIALIZED_HIA') resolve();
        else {
          api
            .post<UserPassword>(
              `/users/${this.user.id}/${this.user.ebicsVersion}/sendHIA`,
              pass
            )
            .then(() => {
              this.$q.notify({
                color: 'positive',
                position: 'bottom-right',
                message: `HIA uploaded successfully for user dn ${this.user.dn}`,
                icon: 'gpp_good',
              });
              resolve();
            })
            .catch((error: Error) => {
              reject(new Error(`HIA failed: ${error.message}`));
            });
        }
      });
    },
    printUserLetters() {
      this.nextStep();
    },
    downloadBankKeys() {
      //Get password for user certificates
      this.promptCertPassword(false)
        .then((pass) => {
          //Upload user keys (INI and/or HIA) depending on user status
          return this.downloadBankKeysHPB({ password: pass }).catch(
            (error: Error) => {
              this.$q.notify({
                color: 'negative',
                position: 'bottom-right',
                message: error.message,
                icon: 'report_problem',
              });
            }
          );
        })
        .then(() => {
          //Refresshing user status on success
          this.refreshUserStatus();
        })
        .catch((error: string) => {
          console.log(error);
        });
    },
    downloadBankKeysHPB(pass: UserPassword) {
      return new Promise<void>((resolve, reject) => {
        api
          .post<UserPassword>(
            `/users/${this.user.id}/${this.user.ebicsVersion}/sendHPB`,
            pass
          )
          .then(() => {
            this.$q.notify({
              color: 'positive',
              position: 'bottom-right',
              message: `HPB downloaded successfully for user dn ${this.user.dn}`,
              icon: 'gpp_good',
            });
            resolve();
          })
          .catch((error: Error) => {
            reject(new Error(`HPB failed: ${error.message}`));
          });
      });
    },
    verifyBankKeys() {
      this.nextStep();
      //this.currentUserStep = UserIniWizzStep.Finish;
    },
    userStatusToStep(userStatus: string): UserIniWizzStep {
      switch (userStatus) {
        case 'CREATED':
          return UserIniWizzStep.CreateUserKeys;
        case 'NEW':
        case 'LOCKED':
        case 'PARTLY_INITIALIZED_INI':
        case 'PARTLY_INITIALIZED_HIA':
          return UserIniWizzStep.UploadUserKeys;
        case 'INITIALIZED':
          return UserIniWizzStep.PrintUserLetters;
        case 'READY':
          return UserIniWizzStep.VerifyBankKeys;
      }
      return UserIniWizzStep.CreateUserKeys;
    },
    stepDone(step: UserIniWizzStep): boolean {
      return step < this.initialUserStep;
    },
  },
  setup(props) {
    const $q = useQuasar();
 
    const user = reactive(props.modelValue);

    function passwordDialog(createPass: boolean): Promise<string> {
      return new Promise<string>((resolve, reject) => {
        $q.dialog({
          title: createPass ? 'Create new password' : 'Enter password',
          message: createPass
            ? 'Create new password for your user certificate'
            : 'Enter password for your user certificate',
          prompt: {
            model: '',
            //isValid: val => (val as string).length > 2,
            type: 'password',
          },
          cancel: true,
          persistent: true,
        })
          .onOk((data: unknown) => {
            console.log(JSON.stringify(data));
            resolve(data as string);
          })
          .onCancel(() => {
            reject('Password entry canceled');
          })
          .onDismiss(() => {
            reject('Password entry dismissed');
          });
      });
    }

    return { passwordDialog, user };
  },
});
</script>
