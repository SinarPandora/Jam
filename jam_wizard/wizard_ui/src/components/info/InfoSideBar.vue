<template>
  <v-navigation-drawer
      app
      clipped
      right
  >
    <v-list>
      <v-list-item
          v-for="(item, idx) in items"
          :key="idx"
          link
      >
        <v-list-item-content>
          <v-list-item-title v-on:click="hello">{{ item }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script>
export default {
  name: "InfoSideBar",
  data: () => ({
    items: [
      "foo",
      "bar",
      "baz"
    ]
  }),
  methods: {
    hello: () => {
      console.log("Hello")
    }
  }
}
</script>

<style scoped>

</style>
