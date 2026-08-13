import { useEffect, useState } from "react";
import { Card, Tabs, Table, Button, Modal, Form, Input, Select, TimePicker, message, Space, Tag, Row } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useNavigate } from "react-router-dom";
import dayjs from "dayjs";
import { useAuth } from "../context/AuthContext";
import {
  adminGetAllUsers,
  adminCreateUser,
  adminUpdateUser,
  adminDeactivateUser,
  getDepartments,
  adminCreateDepartment,
  adminUpdateDepartment,
  getWorkSchedules,
  adminCreateWorkSchedule,
  adminUpdateWorkSchedule,
} from "../service/adminApi";
import type { UserResponse } from "../types/auth";
import type { DepartmentResponse, WorkScheduleResponse } from "../types/admin";
import { getTeamLeaves, approveLeave, rejectLeave } from "../service/managerApi";
import type { LeaveRequestResponse } from "../types/leave";


const AdminPage = () => {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const [users, setUsers] = useState<UserResponse[]>([]);
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [schedules, setSchedules] = useState<WorkScheduleResponse[]>([]);

  const [userModalOpen, setUserModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserResponse | null>(null);
  const [userForm] = Form.useForm();

  const [deptModalOpen, setDeptModalOpen] = useState(false);
  const [editingDept, setEditingDept] = useState<DepartmentResponse | null>(null);
  const [deptForm] = Form.useForm();

  const [scheduleModalOpen, setScheduleModalOpen] = useState(false);
  const [editingSchedule, setEditingSchedule] = useState<WorkScheduleResponse | null>(null);
  const [scheduleForm] = Form.useForm();

  const [pendingLeaves, setPendingLeaves] = useState<LeaveRequestResponse[]>([]);

  const [usersPage, setUsersPage] = useState(0);
  const [usersTotal, setUsersTotal] = useState(0);
  const [roleFilter, setRoleFilter] = useState<string | undefined>(undefined);

  useEffect(() => {
    fetchUsers();
    fetchDepartments();
    fetchSchedules();
    fetchPendingLeaves();
  }, []);

  useEffect(() => {
  fetchUsers();
}, [usersPage, roleFilter]);

  const fetchUsers = async () => {
  try {
    const filters = roleFilter ? [{ field: "role", operator: "EQ" as const, value: roleFilter }] : [];
    const data = await adminGetAllUsers({ page: usersPage, size: 10, filters });
    setUsers(data.content);
    setUsersTotal(data.totalElements);
  } catch {
    message.error("Kullanıcılar yüklenemedi.");
  }
};

  const fetchDepartments = async () => {
    try {
      setDepartments(await getDepartments());
    } catch {
      message.error("Departmanlar yüklenemedi.");
    }
  };

  const fetchSchedules = async () => {
    try {
      setSchedules(await getWorkSchedules());
    } catch {
      message.error("Mesai kuralları yüklenemedi.");
    }
  };

  // --- Kullanıcı işlemleri ---

  const openCreateUser = () => {
    setEditingUser(null);
    userForm.resetFields();
    setUserModalOpen(true);
  };

  const openEditUser = (user: UserResponse) => {
    setEditingUser(user);
    userForm.setFieldsValue({
      firstName: user.firstName,
      lastName: user.lastName,
      role: user.role,
    });
    setUserModalOpen(true);
  };

  const handleUserSubmit = async () => {
    try {
      const values = await userForm.validateFields();
      if (editingUser) {
        await adminUpdateUser(editingUser.id, values);
        message.success("Kullanıcı güncellendi.");
      } else {
        await adminCreateUser(values);
        message.success("Kullanıcı oluşturuldu.");
      }
      setUserModalOpen(false);
      fetchUsers();
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.response?.data?.message || "İşlem başarısız.");
    }
  };

  const handleDeactivate = async (id: number) => {
    try {
      await adminDeactivateUser(id);
      message.success("Kullanıcı pasif hale getirildi.");
      fetchUsers();
    } catch {
      message.error("İşlem başarısız.");
    }
  };

  // --- Departman işlemleri ---

  const openCreateDept = () => {
    setEditingDept(null);
    deptForm.resetFields();
    setDeptModalOpen(true);
  };

  const openEditDept = (dept: DepartmentResponse) => {
    setEditingDept(dept);
    deptForm.setFieldsValue({ name: dept.name, managerId: dept.managerId });
    setDeptModalOpen(true);
  };

  const handleDeptSubmit = async () => {
    try {
      const values = await deptForm.validateFields();
      if (editingDept) {
        await adminUpdateDepartment(editingDept.id, values);
        message.success("Departman güncellendi.");
      } else {
        await adminCreateDepartment(values);
        message.success("Departman oluşturuldu.");
      }
      setDeptModalOpen(false);
      fetchDepartments();
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.response?.data?.message || "İşlem başarısız.");
    }
  };

  // --- Mesai Kuralı işlemleri ---

  const openCreateSchedule = () => {
    setEditingSchedule(null);
    scheduleForm.resetFields();
    setScheduleModalOpen(true);
  };

  const openEditSchedule = (ws: WorkScheduleResponse) => {
    setEditingSchedule(ws);
    scheduleForm.setFieldsValue({
      departmentId: ws.departmentId,
      startTime: dayjs(ws.startTime, "HH:mm:ss"),
      endTime: dayjs(ws.endTime, "HH:mm:ss"),
      toleranceMinutes: ws.toleranceMinutes,
    });
    setScheduleModalOpen(true);
  };

  const handleScheduleSubmit = async () => {
    try {
      const values = await scheduleForm.validateFields();
      const payload = {
        departmentId: values.departmentId,
        startTime: values.startTime.format("HH:mm:ss"),
        endTime: values.endTime.format("HH:mm:ss"),
        toleranceMinutes: values.toleranceMinutes,
      };
      if (editingSchedule) {
        await adminUpdateWorkSchedule(editingSchedule.id, payload);
        message.success("Mesai kuralı güncellendi.");
      } else {
        await adminCreateWorkSchedule(payload);
        message.success("Mesai kuralı oluşturuldu.");
      }
      setScheduleModalOpen(false);
      fetchSchedules();
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.response?.data?.message || "İşlem başarısız.");
    }
  };

  // Manager İzin İşlemleri 
const fetchPendingLeaves = async () => {
  try {
    const data = await getTeamLeaves(0, 50);
    setPendingLeaves(data.content);
  } catch {
    message.error("İzin talepleri yüklenemedi.");
  }
};

const handleApproveLeave = async (id: number) => {
  try {
    await approveLeave(id);
    message.success("İzin talebi onaylandı.");
    fetchPendingLeaves();
  } catch (err: any) {
    message.error(err.response?.data?.message || "Onaylama başarısız.");
  }
};

const handleRejectLeave = async (id: number) => {
  try {
    await rejectLeave(id);
    message.success("İzin talebi reddedildi.");
    fetchPendingLeaves();
  } catch (err: any) {
    message.error(err.response?.data?.message || "Reddetme başarısız.");
  }
};

  // --- Kolonlar ---

  const userColumns: ColumnsType<UserResponse> = [
    { title: "Ad Soyad", key: "name", render: (_, r) => `${r.firstName} ${r.lastName}` },
    { title: "Email", dataIndex: "email", key: "email" },
    {
      title: "Rol",
      dataIndex: "role",
      key: "role",
      render: (v: string) => {
        const colorMap: Record<string, string> = { EMPLOYEE: "blue", MANAGER: "purple", ADMIN: "red" };
        return <Tag color={colorMap[v]}>{v}</Tag>;
      },
    },
    { title: "Departman", dataIndex: "departmentName", key: "departmentName", render: (v) => v ?? "-" },
    { title: "Yönetici", dataIndex: "managerName", key: "managerName", render: (v) => v ?? "-" },
    {
      title: "Aktif mi",
      dataIndex: "active",
      key: "active",
      render: (v: boolean) => (v ? <Tag color="green">Aktif</Tag> : <Tag color="default">Pasif</Tag>),
    },
    {
      title: "İşlem",
      key: "action",
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => openEditUser(record)}>
            Düzenle
          </Button>
          {record.active && (
            <Button size="small" danger onClick={() => handleDeactivate(record.id)}>
              Pasifleştir
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const deptColumns: ColumnsType<DepartmentResponse> = [
    { title: "Departman Adı", dataIndex: "name", key: "name" },
    { title: "Yönetici", dataIndex: "managerFullName", key: "managerFullName", render: (v) => v ?? "-" },
    {
      title: "İşlem",
      key: "action",
      render: (_, record) => (
        <Button size="small" onClick={() => openEditDept(record)}>
          Düzenle
        </Button>
      ),
    },
  ];

  const scheduleColumns: ColumnsType<WorkScheduleResponse> = [
    { title: "Departman", dataIndex: "departmentName", key: "departmentName" },
    { title: "Başlangıç", dataIndex: "startTime", key: "startTime" },
    { title: "Bitiş", dataIndex: "endTime", key: "endTime" },
    { title: "Tolerans (dk)", dataIndex: "toleranceMinutes", key: "toleranceMinutes" },
    {
      title: "İşlem",
      key: "action",
      render: (_, record) => (
        <Button size="small" onClick={() => openEditSchedule(record)}>
          Düzenle
        </Button>
      ),
    },
  ];

const leaveColumns: ColumnsType<LeaveRequestResponse> = [
  { title: "Personel", dataIndex: "userFullName", key: "userFullName" },
  { title: "Başlangıç", dataIndex: "startDate", key: "startDate" },
  { title: "Bitiş", dataIndex: "endDate", key: "endDate" },
  { title: "Tür", dataIndex: "leaveType", key: "leaveType" },
  {
    title: "Durum",
    dataIndex: "status",
    key: "status",
    render: (v: string) => {
      const colorMap: Record<string, string> = { PENDING: "gold", APPROVED: "green", REJECTED: "red" };
      const textMap: Record<string, string> = { PENDING: "Beklemede", APPROVED: "Onaylandı", REJECTED: "Reddedildi" };
      return <Tag color={colorMap[v]}>{textMap[v]}</Tag>;
    },
  },
  {
    title: "İşlem",
    key: "action",
    render: (_, record) =>
      record.status === "PENDING" ? (
        <Space>
          <Button size="small" type="primary" onClick={() => handleApproveLeave(record.id)}>
            Onayla
          </Button>
          <Button size="small" danger onClick={() => handleRejectLeave(record.id)}>
            Reddet
          </Button>
        </Space>
      ) : (
        "-"
      ),
  },
  
];

const managers = users.filter((u) => u.role === "MANAGER");


  return (
    <div style={{ maxWidth: 1100, margin: "40px auto", padding: "0 20px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <h2>Yönetim Paneli</h2>
        <Button onClick={() => { logout(); navigate("/login"); }}>Çıkış Yap</Button>
      </div>

      

      <Card>
        <Tabs
          items={[
            {
              key: "users",
              label: "Kullanıcılar",
              children: (
              <>
                <Row justify="space-between" style={{ marginBottom: 16 }}>
                  <Select
                    placeholder="Role göre filtrele"
                    style={{ width: 200 }}
                    allowClear
                    value={roleFilter}
                    onChange={(value) => {
                      setRoleFilter(value);
                      setUsersPage(0);
                    }}
                    options={[
                      { value: "EMPLOYEE", label: "Employee" },
                      { value: "MANAGER", label: "Manager" },
                    ]}
                  />
                  <Button type="primary" onClick={openCreateUser}>
                    Yeni Kullanıcı
                  </Button>
                </Row>
                <Table
                  columns={userColumns}
                  dataSource={users}
                  rowKey="id"
                  pagination={{
                    current: usersPage + 1,
                    pageSize: 10,
                    total: usersTotal,
                    onChange: (p) => setUsersPage(p - 1),
                  }}
                />
              </>
            ),
            },
            {
              key: "departments",
              label: "Departmanlar",
              children: (
                <>
                  <Button type="primary" style={{ marginBottom: 16 }} onClick={openCreateDept}>
                    Yeni Departman
                  </Button>
                  <Table columns={deptColumns} dataSource={departments} rowKey="id" />
                </>
              ),
            },
            {
              key: "leaves",
              label: "İzin Talepleri",
              children: <Table columns={leaveColumns} dataSource={pendingLeaves} rowKey="id" />,
            },
            {
              key: "schedules",
              label: "Mesai Kuralları",
              children: (
                <>
                  <Button type="primary" style={{ marginBottom: 16 }} onClick={openCreateSchedule}>
                    Yeni Mesai Kuralı
                  </Button>
                  <Table columns={scheduleColumns} dataSource={schedules} rowKey="id" />
                </>
              ),
            },
          ]}
        />
      </Card>

      {/* Kullanıcı Modal */}
      <Modal
        title={editingUser ? "Kullanıcı Düzenle" : "Yeni Kullanıcı"}
        open={userModalOpen}
        onOk={handleUserSubmit}
        onCancel={() => setUserModalOpen(false)}
      >
        <Form form={userForm} layout="vertical">
          <Form.Item name="firstName" label="Ad" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="lastName" label="Soyad" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          {!editingUser && (
            <>
              <Form.Item name="email" label="Email" rules={[{ required: true, type: "email" }]}>
                <Input />
              </Form.Item>
            </>
          )}
          <Form.Item name="role" label="Rol">
            <Select
              options={[
                { value: "EMPLOYEE", label: "Employee" },
                { value: "MANAGER", label: "Manager" },
                { value: "ADMIN", label: "Admin" },
              ]}
            />
          </Form.Item>
          <Form.Item name="departmentId" label="Departman">
            <Select
              allowClear
              options={departments.map((d) => ({ value: d.id, label: d.name }))}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* Departman Modal */}
      <Modal
        title={editingDept ? "Departman Düzenle" : "Yeni Departman"}
        open={deptModalOpen}
        onOk={handleDeptSubmit}
        onCancel={() => setDeptModalOpen(false)}
      >
        <Form form={deptForm} layout="vertical">
          <Form.Item name="name" label="Departman Adı" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="managerId" label="Yönetici">
            <Select
              allowClear
              options={managers.map((u) => ({ value: u.id, label: `${u.firstName} ${u.lastName}` }))}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* Mesai Kuralı Modal */}
      <Modal
        title={editingSchedule ? "Mesai Kuralı Düzenle" : "Yeni Mesai Kuralı"}
        open={scheduleModalOpen}
        onOk={handleScheduleSubmit}
        onCancel={() => setScheduleModalOpen(false)}
      >
        <Form form={scheduleForm} layout="vertical">
          <Form.Item name="departmentId" label="Departman (boş = genel kural)">
            <Select
              allowClear
              options={departments.map((d) => ({ value: d.id, label: d.name }))}
            />
          </Form.Item>
          <Form.Item name="startTime" label="Başlangıç Saati" rules={[{ required: true }]}>
            <TimePicker format="HH:mm" />
          </Form.Item>
          <Form.Item name="endTime" label="Bitiş Saati" rules={[{ required: true }]}>
            <TimePicker format="HH:mm" />
          </Form.Item>
          <Form.Item name="toleranceMinutes" label="Tolerans (dakika)" rules={[{ required: true }]}>
            <Input type="number" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminPage;