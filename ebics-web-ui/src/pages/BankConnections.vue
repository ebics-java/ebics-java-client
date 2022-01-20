<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <q-table
        title="Bank connections"
        :filter="filter"
        :rows="bankConnections"
        :columns="columns"
        row-key="id"
        selection="single"
        :pagination="{rowsPerPage: 10}"
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
            <q-td :style="{ width: '330px' }">
              <div class="q-gutter-sm">
                <q-btn
                  size="sm"
                  color="primary"
                  label="Edit"
                  icon-right="edit"
                  no-caps
                  @click="routeToUserPage(Number(props.key))"
                />
                <q-btn
                  size="sm"
                  color="primary"
                  label="Initialize"
                  icon-right="rotate_right"
                  no-caps
                  @click="routeToInitPage(Number(props.key))"
                />
                <q-btn
                  size="sm"
                  label="Delete"
                  color="accent"
                  icon-right="delete"
                  @click="deleteBankConnection(Number(props.key), props.cols[0].value);"
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
              label="Add Bank Connection"
              icon-right="add"
              no-caps
              @click="routeToUserPage()"
            />
          </div>
        </template>
      </q-table>
    </div>
  </q-page>
</template>

<script lang="ts">
import { BankConnection } from 'components/models';
import { defineComponent } from 'vue';
import useBankConnectionsAPI from 'components/bankconnections'

export default defineComponent({
  name: 'Users',
  components: {},
  data() {
    return {
      confirmDelete: false,
      filter: '',
    };
  },
  methods: {
    /**
     * Route to User page
     * userId
     *  - if given then will be routed with 'id' parameter to edit page
     *  - if undefined will be routed without 'id' parameter to create page
     */
    async routeToUserPage(userId?: number) {
      const routeParams = userId === undefined ? undefined : { id: userId };
      const routeName = userId === undefined ? 'bankconnection/create' : 'bankconnection/edit';
      await this.$router.push({
        name: routeName,
        params: routeParams,
      });
    },
    async routeToInitPage(userId: number) {
      const routeParams = { id: userId };
      const routeName = 'bankconnection/init';
      await this.$router.push({
        name: routeName,
        params: routeParams,
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
  setup() {
    const columns = [
      {
        name: 'name',
        required: true,
        label: 'Name',
        align: 'left',
        field: (row: BankConnection) => row.name,
        sortable: true,
      },
      {
        name: 'ebicsUserId',
        required: true,
        label: 'EBICS User ID',
        align: 'left',
        field: (row: BankConnection) => row.userId,
        sortable: true,
      },
      {
        name: 'ebicsPartnerId',
        required: true,
        label: 'EBICS Partner ID',
        align: 'left',
        field: (row: BankConnection) => row?.partner?.partnerId ?? '',
        sortable: true,
      },
      {
        name: 'ebicsBankName',
        required: true,
        label: 'Bank Name',
        align: 'left',
        field: (row: BankConnection) => row?.partner?.bank?.name ?? '',
        sortable: true,
      },
      {
        name: 'owner',
        required: true,
        label: 'Owner',
        align: 'left',
        field: (row: BankConnection) => row?.creator ?? '',
        sortable: true,
      },
      {
        name: 'shared',
        required: true,
        label: 'Shared',
        align: 'left',
        field: (row: BankConnection) => row?.guestAccess ? 'yes' : 'no',
        sortable: true,
      },
    ];
    const {bankConnections, deleteBankConnection} = useBankConnectionsAPI();

    return { columns, bankConnections, deleteBankConnection };
  },
});
</script>
