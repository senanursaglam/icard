import { useEffect, useState } from "react";
import { Card, Tabs, Table, Button, message, Tag, Space, Modal, Col, Statistic, Row, Spin, Select } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useNavigate } from "react-router-dom";
import {
  getTeam,
  getTeamAttendance,
  getTeamLeaves,
  approveLeave,
  rejectLeave,
  getTeamOvertimeSummary,
} from "../service/managerApi";
import type { UserResponse } from "../types/auth";
import type { AttendanceResponse } from "../types/attendance";
import type { LeaveRequestResponse } from "../types/leave";
import type { TeamOvertimeSummaryResponse, EmployeeDetailResponse } from "../types/manager";
import { getEmployeeDetail } from "../service/managerApi";

const ManagerTeamPage = () => {
  const navigate = useNavigate();

  const [team, setTeam] = useState<UserResponse[]>([]);
  const [attendanceList, setAttendanceList] = useState<AttendanceResponse[]>([]);
  const [attendancePage, setAttendancePage] = useState(0);
  const [attendanceTotal, setAttendanceTotal] = useState(0);
  const [leaves, setLeaves] = useState<LeaveRequestResponse[]>([]);
  const [leavesPage, setLeavesPage] = useState(0);
  const [leavesTotal, setLeavesTotal] = useState(0);
  const [overtimeSummary, setOvertimeSummary] = useState<TeamOvertimeSummaryResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [employeeDetail, setEmployeeDetail] = useState<EmployeeDetailResponse | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [leaveStatusFilter, setLeaveStatusFilter] = useState<string | undefined>(undefined);

  useEffect(() => {
    fetchTeam();
    fetchOvertimeSummary();
  }, []);

  useEffect(() => {
    fetchAttendance();
  }, [attendancePage]);

  useEffect(() => {
    fetchLeaves();
  }, [leavesPage, leaveStatusFilter]);

  const fetchTeam = async () => {
    try {
      const data = await getTeam();
      setTeam(data);
    } catch {
      message.error("Ekip listesi yüklenemedi.");
    }
  };

  const fetchAttendance = async () => {
    setLoading(true);
    try {
      const data = await getTeamAttendance({
        page: attendancePage,
        size: 10,
        sort: "loginTime,desc",
        filters: [],
      });
      setAttendanceList(data.content);
      setAttendanceTotal(data.totalElements);
    } catch {
      message.error("Ekip attendance kayıtları yüklenemedi.");
    } finally {
      setLoading(false);
    }
  };

  const fetchLeaves = async () => {
  try {
    const data = await getTeamLeaves(leavesPage, 10, leaveStatusFilter);
    setLeaves(data.content);
    setLeavesTotal(data.totalElements);
  } catch {
    message.error("İzin talepleri yüklenemedi.");
  }
};

  const fetchOvertimeSummary = async () => {
    try {
      const data = await getTeamOvertimeSummary();
      setOvertimeSummary(data);
    } catch {
      message.error("Fazla mesai özeti yüklenemedi.");
    }
  };

  const handleApprove = async (id: number) => {
    try {
      await approveLeave(id);
      message.success("İzin talebi onaylandı.");
      fetchLeaves();
    } catch (err: any) {
      message.error(err.response?.data?.message || "Onaylama başarısız.");
    }
  };

  const handleReject = async (id: number) => {
    try {
      await rejectLeave(id);
      message.success("İzin talebi reddedildi.");
      fetchLeaves();
    } catch (err: any) {
      message.error(err.response?.data?.message || "Reddetme başarısız.");
    }
  };

  const teamColumns: ColumnsType<UserResponse> = [
    { title: "Ad Soyad", key: "name", render: (_, r) => `${r.firstName} ${r.lastName}` },
    { title: "Email", dataIndex: "email", key: "email" },
    { title: "Departman", dataIndex: "departmentName", key: "departmentName", render: (v) => v ?? "-" },
    { title: "Aktif mi", dataIndex: "active", key: "active", render: (v) => (v ? "Evet" : "Hayır") },
    {
    title: "İşlem",
    key: "action",
    render: (_, record) => (
      <Button size="small" onClick={() => openEmployeeDetail(record.id)}>
        Detay
      </Button>
    ),
  },
  ];

  const attendanceColumns: ColumnsType<AttendanceResponse> = [
    { title: "Personel", dataIndex: "userFullName", key: "userFullName" },
    {
      title: "Giriş Saati",
      dataIndex: "loginTime",
      key: "loginTime",
      render: (v: string) => new Date(v).toLocaleString("tr-TR"),
    },
    {
      title: "Çıkış Saati",
      dataIndex: "logoutTime",
      key: "logoutTime",
      render: (v: string | null) => (v ? new Date(v).toLocaleString("tr-TR") : "-"),
    },
    {
      title: "Çalışılan Süre",
      dataIndex: "workedHours",
      key: "workedHours",
      render: (v: number | null) => (v !== null ? `${v.toFixed(2)} saat` : "-"),
    },
    {
      title: "Durum",
      dataIndex: "status",
      key: "status",
      render: (v: string) => (v === "ACTIVE" ? "Aktif" : "Tamamlandı"),
    },
    {
      title: "Geç Kaldı mı",
      dataIndex: "isLate",
      key: "isLate",
      render: (v: boolean) => (v ? <Tag color="red">Geç</Tag> : <Tag color="green">Zamanında</Tag>),
    },
    {
      title: "Fazla Mesai",
      dataIndex: "overtimeMinutes",
      key: "overtimeMinutes",
      render: (v: number | null) => (v && v > 0 ? `${(v / 60).toFixed(2)} saat` : "-"),
    },
  ];

  const leavesColumns: ColumnsType<LeaveRequestResponse> = [
    { title: "Personel", dataIndex: "userFullName", key: "userFullName" },
    { title: "Başlangıç", dataIndex: "startDate", key: "startDate" },
    { title: "Bitiş", dataIndex: "endDate", key: "endDate" },
    { title: "Tür", dataIndex: "leaveType", key: "leaveType" },
    { title: "Açıklama", dataIndex: "reason", key: "reason", render: (v) => v ?? "-" },
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
            <Button size="small" type="primary" onClick={() => handleApprove(record.id)}>
              Onayla
            </Button>
            <Button size="small" danger onClick={() => handleReject(record.id)}>
              Reddet
            </Button>
          </Space>
        ) : (
          "-"
        ),
    },
  ];

  const overtimeColumns: ColumnsType<TeamOvertimeSummaryResponse> = [
    { title: "Personel", dataIndex: "userFullName", key: "userFullName" },
    { title: "Toplam Fazla Mesai (saat)", dataIndex: "totalOvertimeHours", key: "totalOvertimeHours" },
    { title: "Geç Kalma Sayısı", dataIndex: "lateCount", key: "lateCount" },
  ];

  const openEmployeeDetail = async (employeeId: number) => {
  setDetailModalOpen(true);
  setDetailLoading(true);
  try {
    const data = await getEmployeeDetail(employeeId);
    setEmployeeDetail(data);
  } catch {
    message.error("Personel detayı yüklenemedi.");
  } finally {
    setDetailLoading(false);
  }
};

  return (
    <div style={{ maxWidth: 1100, margin: "40px auto", padding: "0 20px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <h2>Ekibim</h2>
        <Button onClick={() => navigate("/dashboard")}>Dashboard'a Dön</Button>
      </div>

      <Card>
        <Tabs
          items={[
            {
              key: "team",
              label: "Ekip Listesi",
              children: <Table columns={teamColumns} dataSource={team} rowKey="id" pagination={false} />,
            },
            {
              key: "attendance",
              label: "Ekip Attendance",
              children: (
                <Table
                  columns={attendanceColumns}
                  dataSource={attendanceList}
                  rowKey="id"
                  loading={loading}
                  pagination={{
                    current: attendancePage + 1,
                    pageSize: 10,
                    total: attendanceTotal,
                    onChange: (p) => setAttendancePage(p - 1),
                  }}
                />
              ),
            },
            {
              key: "leaves",
              label: "İzin Talepleri",
              children: (
                <>
                  <Select
                    placeholder="Duruma göre filtrele"
                    style={{ width: 200, marginBottom: 16 }}
                    allowClear
                    value={leaveStatusFilter}
                    onChange={(value) => {
                      setLeaveStatusFilter(value);
                      setLeavesPage(0);
                    }}
                    options={[
                      { value: "PENDING", label: "Beklemede" },
                      { value: "APPROVED", label: "Onaylandı" },
                      { value: "REJECTED", label: "Reddedildi" },
                    ]}
                  />
                  <Table
                    columns={leavesColumns}
                    dataSource={leaves}
                    rowKey="id"
                    pagination={{
                      current: leavesPage + 1,
                      pageSize: 10,
                      total: leavesTotal,
                      onChange: (p) => setLeavesPage(p - 1),
                    }}
                  />
                </>
              ),
            },
            {
              key: "overtime",
              label: "Fazla Mesai Özeti",
              children: <Table columns={overtimeColumns} dataSource={overtimeSummary} rowKey="userFullName" pagination={false} />,
            },
          ]}
        />
      </Card>

    <Modal
  title={employeeDetail ? `${employeeDetail.user.firstName} ${employeeDetail.user.lastName} — Detay` : "Detay"}
  open={detailModalOpen}
  onCancel={() => setDetailModalOpen(false)}
  footer={null}
  width={700}
>
  {detailLoading ? (
    <Spin />
  ) : employeeDetail ? (
    <>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}>
          <Card size="small">
            <Statistic title="Toplam Fazla Mesai (saat)" value={employeeDetail.totalOvertimeHours} />
          </Card>
        </Col>
        <Col span={8}>
          <Card size="small">
            <Statistic title="Geç Kalma Sayısı" value={employeeDetail.lateCount} />
          </Card>
        </Col>
        <Col span={8}>
          <Card size="small">
            <Statistic title="Bekleyen İzin Talebi" value={employeeDetail.pendingLeaveCount} />
          </Card>
        </Col>
      </Row>

      <h4>Son Attendance Kayıtları</h4>
      <Table
        size="small"
        columns={[
          {
            title: "Giriş",
            dataIndex: "loginTime",
            key: "loginTime",
            render: (v: string) => new Date(v).toLocaleString("tr-TR"),
          },
          {
            title: "Çıkış",
            dataIndex: "logoutTime",
            key: "logoutTime",
            render: (v: string | null) => (v ? new Date(v).toLocaleString("tr-TR") : "-"),
          },
          {
            title: "Süre",
            dataIndex: "workedHours",
            key: "workedHours",
            render: (v: number | null) => (v !== null ? `${v.toFixed(2)} saat` : "-"),
          },
          {
            title: "Geç mi",
            dataIndex: "isLate",
            key: "isLate",
            render: (v: boolean) => (v ? <Tag color="red">Geç</Tag> : <Tag color="green">-</Tag>),
          },
        ]}
        dataSource={employeeDetail.recentAttendance}
        rowKey="id"
        pagination={false}
      />
    </>
  ) : null}
</Modal>
    </div>
  );
};

export default ManagerTeamPage;