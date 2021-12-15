<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md q-ma-md">
      <q-table
        title="Traces"
        :filter="filter"
        :rows="traces"
        :columns="columns"
        :filter-method="searchTraces"
        row-key="id"
        selection="single"
        v-model:selected="selectedTraceList"
      >
        <template v-slot:header="props">
          <q-tr :props="props">
            <q-th v-for="col in props.cols.filter(c => c.name != 'messageBody')" :key="col.name" :props="props">
              {{ col.label }}
            </q-th>
          </q-tr>
        </template>
        <template v-slot:body="props">
          <q-tr :props="props" @click="props.selected = !props.selected">
            <!-- props.selected = !props.selected -->
            <q-td v-for="col in props.cols.filter(c => c.name != 'messageBody')" :key="col.name" :props="props">
              {{ col.value }}
            </q-td>
          </q-tr>
        </template>
        <template v-slot:top-left>
          <q-input
            borderless
            dense
            debounce="300"
            v-model="filter"
            placeholder="Search"
          >
            <template v-slot:append>
              <q-icon name="search" />
            </template>
          </q-input>
        </template>
      </q-table>
      <v-ace-editor
        v-bind:value="selectedTrace?.messageBody ?? ''"
        lang="xml"
        theme="clouds"
        style="height: 300px"
        :printMargin="false"
      />
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import useTracesAPI from 'components/traces';
import { TraceEntry } from 'components/traces';
import { VAceEditor } from 'vue3-ace-editor';
import 'ace-builds/src-noconflict/mode-xml';
import 'ace-builds/src-noconflict/theme-clouds';

export default defineComponent({
  components: {VAceEditor},  
  name: 'Traces',
  data() {
    return {
      filter: '',
    };
  },
  setup() {
    const columns = [
      {
        name: 'id',
        required: true,
        label: 'Id',
        align: 'left',
        field: (row: TraceEntry) => row.id,
        sortable: true,
      },
      {
        name: 'dateTime',
        required: true,
        label: 'Date/Time',
        align: 'left',
        field: (row: TraceEntry) => row.dateTime,
        sortable: true,
      },
      {
        name: 'ebicsUser',
        required: true,
        label: 'EBICS User',
        align: 'left',
        field: (row: TraceEntry) => row.user.userId,
        sortable: true,
      },
      {
        name: 'ebicsCustomer',
        required: true,
        label: 'EBICS Customer',
        align: 'left',
        field: (row: TraceEntry) => row.user.partner.partnerId,
        sortable: true,
      },
      {
        name: 'creator',
        required: true,
        label: 'User',
        align: 'left',
        field: (row: TraceEntry) => row.creator,
        sortable: true,
      },
      //
      {
        name: 'messageBody',
        label: 'Message Body',
        align: 'left',
        field: (row: TraceEntry) => row.messageBody,
        sortable: true,
      },
    ];
    const { traces } = useTracesAPI();
    const selectedTraceList = ref<TraceEntry[]>([]);
    const selectedTrace = computed(():TraceEntry | undefined => {
      return selectedTraceList.value?.length ? selectedTraceList.value[0] as TraceEntry: undefined;
    });
    return { columns, traces, selectedTrace, selectedTraceList };
  },
});
</script>
