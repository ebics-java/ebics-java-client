<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <h5>Banks</h5>
      <q-table
        title="Banks"
        :filter="filter"
        :rows="banks"
        :columns="columns"
        row-key="id"
        selection="single"
      >
        <template v-slot:header="props">
          <q-tr :props="props">
            <q-th v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.label }}
            </q-th>
            <q-th auto-width></q-th>
          </q-tr>
        </template>
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.value }}
            </q-td>
            <q-td :style="{ width: '220px' }">
              <div class="q-gutter-sm">
                <q-btn
                  size="sm"
                  color="primary"
                  label="Edit"
                  icon-right="edit"
                  no-caps
                  @click="routeToBankPage(Number(props.key))"
                />
                <q-btn
                  size="sm"
                  label="Delete"
                  color="accent"
                  icon-right="delete"
                  @click="
                    deleteBankDialog.show = true;
                    deleteBankDialog.id = Number(props.key);
                    deleteBankDialog.name = props.cols[0].value;
                  "
                />
              </div>
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
        <template v-slot:top-right>
          <div class="q-pa-md q-gutter-sm">
            <q-btn
              color="primary"
              label="Add Bank"
              icon-right="add"
              no-caps
              @click="routeToBankPage()"
            />
            <q-btn
              color="primary"
              icon-right="archive"
              label="Export to csv"
              no-caps
              @click="exportTable"
            />
          </div>
        </template>
      </q-table>
      <q-dialog v-model="deleteBankDialog.show" persistent>
        <q-card>
          <q-card-section class="row items-center">
            <q-avatar icon="delete" color="primary" text-color="white" />
            <span class="q-ml-sm">
              Do you want to delete selected bank:
              {{ deleteBankDialog.name }}.
            </span>
          </q-card-section>

          <q-card-actions align="right">
            <q-btn flat label="Cancel" color="primary" v-close-popup />
            <q-btn
              flat
              label="Delete"
              color="primary"
              v-close-popup
              @click="deleteBank(this.deleteBankDialog.id)"
            />
          </q-card-actions>
        </q-card>
      </q-dialog>
    </div>
  </q-page>
</template>

<script lang="ts">
import { api } from 'boot/axios';
import { Bank, DeleteConfirmDialog } from 'components/models';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'Banks',
  components: {},
  data() {
    return {
      confirmDelete: false,
      deleteBankDialog: {
        show: false,
      } as DeleteConfirmDialog,
      filter: '',
      banks: [
        {
          id: 1,
          bankURL: 'http://ubs.com/ebics',
          name: 'UBC CH PROD',
          hostId: 'EXUBSCH',
        } as Bank,
        {
          id: 2,
          bankURL: 'http://zkb.ch/ebics',
          name: 'ZKB PROD',
          hostId: 'EXZKBCH',
        } as Bank,
      ],
    };
  },
  methods: {
    loadData() {
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
    deleteBank(bankId: number) {
      api
        .delete<Bank[]>(`/banks/${bankId}`)
        .then(() => {
          this.$q.notify({
            color: 'positive',
            position: 'bottom-right',
            message: 'Delete bank done',
            icon: 'report_info',
          });
          this.loadData()
        }).catch((error: Error) => {
          this.$q.notify({
            color: 'negative',
            position: 'bottom-right',
            message: `Delete failed: ${error.message}`,
            icon: 'report_info',
          });
        });
    },
    /**
     * Route to Bank page
     * bankId
     *  - if given then will be routed with 'id' parameter to edit page
     *  - if undefined will be routed without 'id' parameter to create page
     */
    async routeToBankPage(bankId?: number) {
      const routeParams = bankId === undefined ? undefined : { id: bankId };
      const routeName = bankId === undefined ? 'bank/create' : 'bank/edit';
      const action = bankId === undefined ? 'create' : 'edit';
      await this.$router.push({
        name: routeName,
        params: routeParams,
        query: { action: action },
      });
    },
    exportTable() {
      // naive encoding to csv format
      this.$q.notify({
        color: 'positive',
        position: 'bottom-right',
        message: 'Exporting table data',
        icon: 'report_info',
      });
    },
  },
  mounted() {
    this.loadData();
  },
  setup() {
    const columns = [
      {
        name: 'name',
        required: true,
        label: 'Bank name',
        align: 'left',
        field: (row: Bank) => row.name,
        sortable: true,
      },
      {
        name: 'url',
        required: true,
        label: 'URL',
        align: 'left',
        field: (row: Bank) => row.bankURL,
        sortable: true,
      },
      {
        name: 'hostId',
        required: true,
        label: 'EBICS Host Id',
        align: 'left',
        field: (row: Bank) => row.hostId,
        sortable: true,
      },
    ];
    return { columns };
  },
});
</script>
