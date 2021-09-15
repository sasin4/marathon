<template>

<v-data-table
    :headers="headers"
    :items="dashboard"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'Dashboard',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "applicantName", value: "applicantName" },
            { text: "phoneNo", value: "phoneNo" },
            { text: "address", value: "address" },
            { text: "status", value: "status" },
            { text: "topSize", value: "topSize" },
            { text: "bottomSize", value: "bottomSize" },
            { text: "amount", value: "amount" },
            { text: "deliveryStatus", value: "deliveryStatus" },
            { text: "payStatus", value: "payStatus" },
        ],
        dashboard : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/dashboards')

      this.dashboard = temp.data._embedded.dashboards;

    },
    methods: {
    }
  }
</script>

