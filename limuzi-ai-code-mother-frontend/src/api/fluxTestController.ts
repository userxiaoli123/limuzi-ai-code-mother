// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /flux/test/execute-flux */
export async function executeWorkflowWithFlux1(options?: { [key: string]: any }) {
  return request<string[]>('/flux/test/execute-flux', {
    method: 'GET',
    ...(options || {}),
  })
}
