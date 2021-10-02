<template>
  <div>
    <q-list bordered padding>
      <div v-if="displaySection('General')">
        <q-item-label header>General Settings</q-item-label>
        <!--boolean-option
        :disable="true"
        label="Upload on drop"
        hint="Enable uploading of files after dropping for 'Simple file upload'"
        v-model="userSettings.uploadOnDrop"
      /-->
        <boolean-option
          label="Tester settings"
          hint="Enable smart adjustments of uploaded files"
          v-model="userSettings.testerSettings"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="Adjust file authomatically"
          hint="Apply smart adjustmets as bellow for every uploaded file automatically, if disabled you can still apply adjustmets explicitelly"
          v-model="userSettings.adjustmentOptions.applyAutomatically"
        />
        <q-separator spaced />
      </div>
      <div v-if="displaySection('ContentOptions.Pain.00x')">
        <q-item-label header>Smart adjustments for Pain.00x</q-item-label>
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="msgId"
          hint="unique id based on current timestamp and user id"
          v-model="userSettings.adjustmentOptions.pain00x.msgId"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="pmtInfId"
          hint="unique id based on current timestamp, user id and B-Level"
          v-model="userSettings.adjustmentOptions.pain00x.pmtInfId"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="endToEndId"
          hint="unique id based on current timestamp, user id and B/C-Level"
          v-model="userSettings.adjustmentOptions.pain00x.endToEndId"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="instrId"
          hint="unique id based on current timestamp, user id and B/C-Level"
          v-model="userSettings.adjustmentOptions.pain00x.instrId"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="UETR for GPI"
          :hint="`unique UETR id based on random seed: ${this.uetr()}`"
          v-model="userSettings.adjustmentOptions.pain00x.uetr"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="creDtTm"
          :hint="`actual date-time in ISO format: ${new Date().toISOString()}`"
          v-model="userSettings.adjustmentOptions.pain00x.creDtTm"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="reqdExctnDt"
          :hint="`actual date in YYYY-MM-DD format: ${this.currentDate()}`"
          v-model="userSettings.adjustmentOptions.pain00x.reqdExctnDt"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="nbOfTrxs"
          hint="recalculates number of transaction based on C-Levels"
          v-model="userSettings.adjustmentOptions.pain00x.nbOfTrxsCalc"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="ctrlSum"
          hint="recalculates control sum based on C-Level amouths"
          v-model="userSettings.adjustmentOptions.pain00x.ctrlSumCalc"
        />
      </div>
      <q-separator
        spaced
        v-if="
          displaySection('ContentOptions.Pain.00x') &&
          displaySection('ContentOptions.Swift')
        "
      />
      <div v-if="displaySection('ContentOptions.Swift')">
        <q-item-label header>Smart adjustments for MT101</q-item-label>
        <boolean-option
          :disable="!userSettings.testerSettings"
          label=":20 (Message ID)"
          hint="unique id based on prefix and current timestamp (or random)"
          v-model="userSettings.adjustmentOptions.swift.f20"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label=":21 (Transaction ID)"
          hint="unique id based on prefix and current timestamp (or random)"
          v-model="userSettings.adjustmentOptions.swift.f21"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label=":30 (Requested Execution Date)"
          hint="Todays date"
          v-model="userSettings.adjustmentOptions.swift.f30"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="UETR for GPI"
          :hint="`unique UETR id based on random seed: ${this.uetr()}`"
          v-model="userSettings.adjustmentOptions.swift.uetr"
        />
        <boolean-option
          :disable="!userSettings.testerSettings"
          label="Random IDs"
          :hint="`IDs for fields :20 and :21 are based on random generator: ${makeUniqueId(
            16
          )}`"
          v-model="userSettings.adjustmentOptions.swift.randomIds"
        />
      </div>
    </q-list>
    <div class="q-ma-md">
      <q-btn
        v-if="saveButton"
        label="Save settings"
        color="primary"
        @click="saveUserSettings"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import BooleanOption from 'src/components/BooleanOption.vue';
import useTextUtils from './text-utils';
import useUserSettingsAPI from './user-settings';
import { uuid } from 'vue-uuid';

export default defineComponent({
  name: 'ContentAdjustmenOption',
  components: { BooleanOption },
  props: {
    sectionFilter: {
      type: String,
      requred: false,
      default: '',
    },
    saveButton: {
      type: Boolean,
      requred: false,
      default: false,
    },
  },
  methods: {
    displaySection(sectionName: string): boolean {
      return (
        this.sectionFilter == '' || sectionName.includes(this.sectionFilter)
      );
    },
    uetr(): string {
      return uuid.v4();
    },
  },
  setup() {
    const { saveUserSettings, userSettings } = useUserSettingsAPI();
    const { currentDate, makeUniqueId } = useTextUtils();
    return { currentDate, makeUniqueId, userSettings, saveUserSettings };
  },
});
</script>
