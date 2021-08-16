<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <h5>Users</h5>
      <q-table
        title="Users"
        :filter="filter"
        :rows="users"
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
                  @click="routeToUserPage(Number(props.key))"
                />
                <q-btn
                  size="sm"
                  label="Delete"
                  color="accent"
                  icon-right="delete"
                  @click="
                    deleteUserDialog.show = true;
                    deleteUserDialog.id = Number(props.key);
                    deleteUserDialog.name = props.cols[0].value;
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
              label="Add User"
              icon-right="add"
              no-caps
              @click="routeToUserPage()"
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
      <q-dialog v-model="deleteUserDialog.show" persistent>
        <q-card>
          <q-card-section class="row items-center">
            <q-avatar icon="delete" color="primary" text-color="white" />
            <span class="q-ml-sm">
              Do you want to delete selected user:
              {{ deleteUserDialog.name }}.
            </span>
          </q-card-section>

          <q-card-actions align="right">
            <q-btn flat label="Cancel" color="primary" v-close-popup />
            <q-btn
              flat
              label="Delete"
              color="primary"
              v-close-popup
              @click="deleteUser(this.deleteUserDialog.id)"
            />
          </q-card-actions>
        </q-card>
      </q-dialog>
    </div>
  </q-page>
</template>

<script lang="ts">
import { api } from 'boot/axios';
import { User, DeleteConfirmDialog } from 'components/models';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'Users',
  components: {},
  data() {
    return {
      confirmDelete: false,
      deleteUserDialog: {
        show: false,
      } as DeleteConfirmDialog,
      filter: '',
      users: [
        {
          id: 1,
          name: 'TestUser1',
        } as User,
      ],
    };
  },
  methods: {
    loadData() {
      api
        .get<User[]>('bankconnections')
        .then((response) => {
          this.users = response.data;
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
    deleteUser(userId: number) {
      api
        .delete<User>(`bankconnections/${userId}`)
        .then(() => {
          this.$q.notify({
            color: 'positive',
            position: 'bottom-right',
            message: 'Delete user done',
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
     * Route to User page
     * userId
     *  - if given then will be routed with 'id' parameter to edit page
     *  - if undefined will be routed without 'id' parameter to create page
     */
    async routeToUserPage(userId?: number) {
      const routeParams = userId === undefined ? undefined : { id: userId };
      const routeName = userId === undefined ? 'user/create' : 'user/edit';
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
  mounted() {
    this.loadData();
  },
  setup() {
    const columns = [
      {
        name: 'name',
        required: true,
        label: 'User name',
        align: 'left',
        field: (row: User) => row.name,
        sortable: true,
      },
      {
        name: 'ebicsUserId',
        required: true,
        label: 'EBICS User ID',
        align: 'left',
        field: (row: User) => row.userId,
        sortable: true,
      },
      {
        name: 'ebicsPartnerId',
        required: true,
        label: 'EBICS Partner ID',
        align: 'left',
        field: (row: User) => row?.partner?.partnerId ?? '',
        sortable: true,
      },
      {
        name: 'ebicsBankName',
        required: true,
        label: 'Bank Name',
        align: 'left',
        field: (row: User) => row?.partner?.bank?.name ?? '',
        sortable: true,
      },
      {
        name: 'hostId',
        required: true,
        label: 'User DN',
        align: 'left',
        field: (row: User) => row.dn,
        sortable: true,
      },
    ];
    return { columns };
  },
});
</script>
