<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md" style="max-width: 500px">
      <div class="q-gutter-md">
        <q-card class="my-card q-mt-md">
          <q-card-section>
            <div class="q-gutter-md">
              <q-field borderless label="Bank Connection Name" stack-label>
                <template v-slot:control>
                  <div class="self-center full-width no-outline" tabindex="0">
                    {{ bankConnection.name }}
                  </div>
                </template>
              </q-field>
              <q-field borderless label="EBICS User ID" stack-label>
                <template v-slot:control>
                  <div class="self-center full-width no-outline" tabindex="0">
                    {{ bankConnection.userId }}
                  </div>
                </template>
              </q-field>
              <q-field borderless label="EBICS Partner ID" stack-label>
                <template v-slot:control>
                  <div class="self-center full-width no-outline" tabindex="0">
                    {{ bankConnection.partner.partnerId }}
                  </div>
                </template>
              </q-field>
            </div>
          </q-card-section>
        </q-card>

        <q-stepper
          v-model="actualWizardStep"
          color="primary"
          ref="wizz"
          animated
          vertical
        >
          <q-step
            :name="UserIniWizzStep.CreateUserKeys"
            title="Create user keys"
            icon="forward_to_inbox"
            :done="isStepDone(UserIniWizzStep.CreateUserKeys)"
          >
            Continue in order to create user private public keys. You can
            optionally set PIN to protect access to your private keys.
            <div class="q-pa-sm">
              <q-input
                filled
                v-model="bankConnection.dn"
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
                v-model="bankConnection.usePassword"
                label="Protect your private keys with password (2FA)"
              />
            </div>
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
            :done="isStepDone(UserIniWizzStep.UploadUserKeys)"
          >
            Continue in order to create user keys and send them to the bank
            using the entered EBICS parameters (bank url, user, customer). For
            sending of the keys INI and HIA administrative ordertypes will be
            used.
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
            :done="isStepDone(UserIniWizzStep.PrintUserLetters)"
          >
            In order to activate the this EBICS user you have to provide bellow
            generated hash keys to your bank. The bank will check provided hash
            keys and activate the EBICS bankConnection.
            <q-list v-if="letters" bordered padding class="rounded-borders">
              <q-item v-ripple>
                <q-item-section>
                  <q-item-label lines="1">Signature (A005)</q-item-label>
                  <q-item-label caption>{{
                    letters.signature.hash
                  }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn
                    class="gt-xs"
                    size="15px"
                    flat
                    dense
                    icon="content_copy"
                    @click="copyToClipboard(letters?.signature.hash ?? '')"
                  ></q-btn>
                </q-item-section>
              </q-item>
              <q-item v-ripple>
                <q-item-section>
                  <q-item-label lines="1">Authentication (X002)</q-item-label>
                  <q-item-label caption>{{
                    letters.authentication.hash
                  }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn
                    class="gt-xs"
                    size="15px"
                    flat
                    dense
                    icon="content_copy"
                    @click="copyToClipboard(letters?.authentication.hash ?? '')"
                  ></q-btn>
                </q-item-section>
              </q-item>
              <q-item v-ripple>
                <q-item-section>
                  <q-item-label lines="1">Encryption (E002)</q-item-label>
                  <q-item-label caption>{{
                    letters.encryption.hash
                  }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn
                    class="gt-xs"
                    size="15px"
                    flat
                    dense
                    icon="content_copy"
                    @click="copyToClipboard(letters?.encryption.hash ?? '')"
                  ></q-btn>
                </q-item-section>
              </q-item>
            </q-list>

            <q-btn
              v-else
              @click="refreshUserLetters()"
              label="Refresh User Letters"
              color="primary"
              class="q-ml-sm"
              icon="print"
            ></q-btn>
            <q-stepper-navigation>
              <q-btn
                @click="nextStep()"
                color="primary"
                label="Continue"
              ></q-btn>
            </q-stepper-navigation>
          </q-step>

          <q-step
            :name="UserIniWizzStep.DownloadBankKeys"
            title="Download bank keys"
            icon="download"
            :done="isStepDone(UserIniWizzStep.DownloadBankKeys)"
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
            :done="isStepDone(UserIniWizzStep.VerifyBankKeys)"
          >
            Verify bellow downloaded bank keys with the one provided by your
            bank during onboarding. In case they match, is your connection ready
            to use. If they keys DON'T match this connection can't be trussted -
            identity of the bank is not valid. Download the bank keys again, or
            reset connection and initialize again.
            <q-list bordered padding class="rounded-borders">
              <q-item v-ripple>
                <q-item-section>
                  <q-item-label lines="1">Authentication (X002)</q-item-label>
                  <q-item-label caption>{{
                    bankConnection.partner.bank.keyStore.x002DigestHex
                  }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn
                    class="gt-xs"
                    size="15px"
                    flat
                    dense
                    icon="content_copy"
                    @click="
                      copyToClipboard(
                        bankConnection.partner.bank.keyStore.x002DigestHex
                      )
                    "
                  ></q-btn>
                </q-item-section>
              </q-item>
              <q-item v-ripple>
                <q-item-section>
                  <q-item-label lines="1">Encryption (E002)</q-item-label>
                  <q-item-label caption>{{
                    bankConnection.partner.bank.keyStore.e002DigestHex
                  }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn
                    class="gt-xs"
                    size="15px"
                    flat
                    dense
                    icon="content_copy"
                    @click="
                      copyToClipboard(
                        bankConnection.partner.bank.keyStore.e002DigestHex
                      )
                    "
                  ></q-btn>
                </q-item-section>
              </q-item>
            </q-list>
            <q-stepper-navigation>
              <q-btn
                @click="downloadBankKeys()"
                color="primary"
                label="Download bank keys again"
              ></q-btn>
            </q-stepper-navigation>
          </q-step>
        </q-stepper>
        <q-space />
        <q-card class="my-card q-mt-md">
          <q-card-section>
            <q-banner inline-actions class="text-white bg-red">
              Do you want to reset the user status?
              <template v-slot:action>
                <q-btn
                  @click="resetUserStatus()"
                  flat
                  color="white"
                  label="Reset"
                />
              </template>
            </q-banner>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { ref, defineComponent } from 'vue';
import {
  UserIniWizzStep,
  AdminOrderType,
  UserLettersResponse,
} from 'components/models';
import { QStepper } from 'quasar';
import useBankConnectionAPI from 'components/bankconnection';
import useBankConnectionInitializationAPI from 'components/bankconnection-init';
import usePasswordAPI from 'components/password-api';
import useDialogs from 'components/dialogs';
import { copyToClipboard } from 'quasar';

export default defineComponent({
  name: 'UserInitalizationWizard',
  props: {
    id: {
      type: Number,
      required: true,
    },
  },
  setup(props) {
    const { bankConnection, refreshUserData } = useBankConnectionAPI(props.id);
    const { confirmDialog } = useDialogs();
    const { promptCertPassword, resetCertPassword } = usePasswordAPI();

    const {
      actualWizardStep,
      isStepDone,
      createUserKeysRequest,
      ebicsAdminTypeRequest,
      resetUserStatusRequest,
      getUserLetters,
    } = useBankConnectionInitializationAPI(bankConnection);

    const downloadBankKeys = async (): Promise<void> => {
      try {
        //Download bank keys using HPB order type
        await ebicsAdminTypeRequest(AdminOrderType.HPB);
        //Refresshing user status on success
        await refreshUserData();
      } catch (error) {
        console.log(error);
      }
    };

    const resetUserStatus = async (): Promise<void> => {
      try {
        const del = await confirmDialog(
          'Confirm reset',
          'Do you really want to reset bank connection? (it must be then newly initialized in order to upload/download files)'
        );
        if (del) {
          //Get password for user certificates
          await resetUserStatusRequest();
          //Reset the password for certificates
          resetCertPassword(bankConnection.value);
          //Refresshing user status on success
          await refreshUserData();
        }
      } catch (error) {
        console.log(error);
      }
    };

    /*
      Create EBICS User Keys
    */
    const createUserKeys = async (): Promise<void> => {
      try {
        //Upload user keys (INI and/or HIA) depending on user status
        await createUserKeysRequest();
        //Refresshing user status on success
        await refreshUserData();
      } catch (error) {
        console.log(error);
      }
    };

    const letters = ref<UserLettersResponse | undefined>(undefined);
    const refreshUserLetters = async (): Promise<void> => {
      letters.value = await getUserLetters();
    };
    const uploadUserKeys = async (): Promise<void> => {
      try {
        //Create/Refresh user letters
        await refreshUserLetters();
        //Execute INI request
        await ebicsAdminTypeRequest(AdminOrderType.INI);
        //Execute HIA request
        await ebicsAdminTypeRequest(AdminOrderType.HIA);
        //Refresh user data
        await refreshUserData();
      } catch (error) {
        console.log(error);
      }
    };

    const wizz = ref<QStepper | null>(null);
    //Next step of the initialization wizard
    const nextStep = (): void => {
      wizz.value?.next();
    };

    return {
      bankConnection,
      refreshUserData,
      actualWizardStep,
      isStepDone,
      createUserKeysRequest,
      ebicsAdminTypeRequest,
      resetUserStatusRequest,
      getUserLetters,
      promptCertPassword,
      resetCertPassword,
      confirmDialog,
      copyToClipboard,
      downloadBankKeys,
      resetUserStatus,
      createUserKeys,
      uploadUserKeys,
      letters,
      UserIniWizzStep,
      nextStep,
      refreshUserLetters,
      wizz,
    };
  },
});
</script>
