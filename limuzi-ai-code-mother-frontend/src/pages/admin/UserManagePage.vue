<template>

  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
  </div>


  <a-table :columns="columns" :data-source="data" :pagination="pagination" @change="doTableChange">
    <template #headerCell="{ column }">
      <template v-if="column.key === 'name'">
        <span>
          <smile-outlined />
          Name
        </span>
      </template>
    </template>
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'userAvatar'">
        <a-image :src="record.userAvatar" :width="80" />
      </template>
      <template v-else-if="column.dataIndex === 'userRole'">
        <div v-if="record.userRole === 'admin'">
          <a-tag color="green">管理员</a-tag>
        </div>
        <div v-else>
          <a-tag color="blue">普通用户</a-tag>
        </div>
      </template>
      <template v-else-if="column.dataIndex === 'createTime'">
        {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-else-if="column.dataIndex === 'updateTime'">
        {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-else-if="column.key === 'action'">
        <a-space>
          <a-button type="primary" @click="openEdit(record)">编辑</a-button>
          <a-button danger @click="doDelete(record.id)">删除</a-button>
        </a-space>
      </template>
    </template>
  </a-table>

  <!-- 编辑用户弹窗 -->
  <a-modal v-model:open="editVisible" title="编辑用户" :confirm-loading="editLoading" @ok="handleEditOk"
    @cancel="handleEditCancel">
    <a-form :model="editForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="ID">
        <a-input v-model:value="editForm.id" disabled />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="editForm.userName" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item label="头像">
        <a-input v-model:value="editForm.userAvatar" placeholder="请输入头像链接" />
      </a-form-item>
      <a-form-item label="简介">
        <a-input v-model:value="editForm.userProfile" placeholder="请输入简介" />
      </a-form-item>
      <a-form-item label="角色">
        <a-select v-model:value="editForm.userRole" placeholder="请选择角色">
          <a-select-option value="user">普通用户</a-select-option>
          <a-select-option value="admin">管理员</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { deleteUser, listUserVoByPage, updateUser } from '@/api/userController';
import { SmileOutlined, DownOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';
import dayjs from "dayjs"
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]


const data = ref<API.UserVO[]>([])
const total = ref(0)
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 5,
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 5,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 获取数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}


// 编辑相关逻辑
const editVisible = ref(false)
const editLoading = ref(false)
const emptyEditForm: API.UserUpdateRequest = {
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: undefined as unknown as string,
}
const editForm = reactive<API.UserUpdateRequest>({ ...emptyEditForm })

const openEdit = (record: API.UserVO) => {
  editForm.id = record.id
  editForm.userName = record.userName
  editForm.userAvatar = record.userAvatar
  editForm.userProfile = record.userProfile
  editForm.userRole = record.userRole
  editVisible.value = true
}

const handleEditCancel = () => {
  editVisible.value = false
}

const handleEditOk = async () => {
  if (!editForm.id) {
    message.error('用户ID缺失')
    return
  }
  editLoading.value = true
  try {
    const res = await updateUser({
      id: editForm.id,
      userName: editForm.userName,
      userAvatar: editForm.userAvatar,
      userProfile: editForm.userProfile,
      userRole: editForm.userRole,
    })
    if (res.data.code === 0) {
      message.success('更新成功')
      editVisible.value = false
      fetchData()
    } else {
      message.error('更新失败，' + (res.data.message || '请稍后重试'))
    }
  } finally {
    editLoading.value = false
  }
}



// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>
