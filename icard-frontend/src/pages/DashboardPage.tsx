import { useEffect, useRef, useState } from "react";
import { Card, Col, Row, Table, Statistic, Button, Spin, message, Space, Tag, Modal, Form, Input } from "antd";
import { Column } from "@ant-design/charts";
import { useAuth } from "../context/AuthContext";
import { getActiveStatus, getDashboardStats, getWeeklyChart } from "../service/attendanceApi";
import type { AttendanceResponse, DailyHoursResponse, DashboardStatsResponse } from "../types/attendance";
import type { ColumnsType } from "antd/es/table";
import { useNavigate } from "react-router-dom";
import { checkOutAttendance } from "../service/attendanceApi";
import { Select, DatePicker } from "antd";
import { filterAttendance } from "../service/attendanceApi";
import type { AttendanceFilterRequest, FilterCriteria } from "../types/attendance";
import dayjs from "dayjs";
import { CaretUpOutlined, CaretDownOutlined } from "@ant-design/icons";
import type { LeaveRequestResponse } from "../types/leave";
import { createLeaveRequest, getMyLeaves } from "../service/leaveApi";
import { getDepartments } from "../service/adminApi";
import { getDepartmentWeeklyChart } from "../service/attendanceApi";
import type { DepartmentResponse } from "../types/admin";

const { RangePicker } = DatePicker;
const DashboardPage = () => {
  const { user, logout } = useAuth();

  const [stats, setStats] = useState<DashboardStatsResponse | null>(null);
  const [attendanceList, setAttendanceList] = useState<AttendanceResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [loading, setLoading] = useState(true);
  const [weeklyChart, setWeeklyChart] = useState<DailyHoursResponse[]>([]);
  const [weekRange, setWeekRange] = useState<{ start: string; end: string } | null>(null);
  const [statusFilter, setStatusFilter] = useState<string | undefined>(undefined);
  const [dateRange, setDateRange] = useState<[string, string] | null>(null);
  const [sortOption, setSortOption] = useState<string>("loginTime,desc");
  const [weekOffset, setWeekOffset] = useState(0);
  const [myLeaves, setMyLeaves] = useState<LeaveRequestResponse[]>([]);
  const [leaveModalOpen, setLeaveModalOpen] = useState(false);
  const [leaveForm] = Form.useForm();
  const [lateFilter, setLateFilter] = useState<string | undefined>(undefined);
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [selectedDepartmentId, setSelectedDepartmentId] = useState<number | undefined>(undefined);


  useEffect(() => {
  fetchStats();
}, []);

useEffect(() => {
  fetchWeeklyChart();
}, [weekOffset]);

useEffect(() => {
  fetchAttendance();
}, [page, pageSize, statusFilter, dateRange, sortOption, lateFilter]);


const navigate = useNavigate();

const [hasActiveAttendance, setHasActiveAttendance] = useState(false);

const fetchActiveStatus = async () => {
  try {
    const data = await getActiveStatus();
    setHasActiveAttendance(data.active);
  } catch {
    message.error("Durum bilgisi alınamadı.");
  }
};

useEffect(() => {
  fetchActiveStatus();
}, []);

const handleCheckOut = async () => {
  try {
    await checkOutAttendance();
    message.success("Çıkış yapıldı.");
    fetchAttendance();
    fetchStats();
    fetchWeeklyChart();
    fetchActiveStatus();
  } catch (err: any) {
    message.error(err.response?.data?.message || "Çıkış yapılamadı.");
  }
};

useEffect(() => {
  fetchMyLeaves();
}, []);

const fetchMyLeaves = async () => {
  try {
    const data = await getMyLeaves();
    setMyLeaves(data);
  } catch {
    message.error("İzin talepleriniz yüklenemedi.");
  }
};

const handleLeaveSubmit = async () => {
  try {
    const values = await leaveForm.validateFields();
    const [start, end] = values.dateRange;

    await createLeaveRequest({
      startDate: start.format("YYYY-MM-DD"),
      endDate: end.format("YYYY-MM-DD"),
      leaveType: values.leaveType,
      reason: values.reason,
    });

    message.success("İzin talebiniz oluşturuldu.");
    setLeaveModalOpen(false);
    leaveForm.resetFields();
    fetchMyLeaves();
  } catch (err: any) {
    if (err?.errorFields) return;
    message.error(err.response?.data?.message || "İzin talebi oluşturulamadı.");
  }
};


const fetchWeeklyChart = async () => {
  try {
    const data = selectedDepartmentId
      ? await getDepartmentWeeklyChart(selectedDepartmentId, weekOffset)
      : await getWeeklyChart(weekOffset);
    setWeeklyChart(data.days);
    setWeekRange({ start: data.weekStart, end: data.weekEnd });
  } catch {
    message.error("Grafik verisi yüklenemedi.");
  }
};

  const fetchStats = async () => {
    try {
      const data = await getDashboardStats();
      setStats(data);
    } catch {
      message.error("İstatistikler yüklenemedi.");
    }
  };

const requestIdRef = useRef(0);

const fetchAttendance = async () => {
  const currentRequestId = ++requestIdRef.current;
  setLoading(true);

  try {
    const filters: FilterCriteria[] = [];

    if (statusFilter) {
      filters.push({ field: "status", operator: "EQ", value: statusFilter });
    }

    if (dateRange) {
      filters.push({
        field: "loginTime",
        operator: "DATE_BETWEEN",
        values: dateRange,
      });
    }

    if (lateFilter) {
      filters.push({ field: "isLate", operator: "EQ", value: lateFilter });
    }

    const request: AttendanceFilterRequest = {
      page,
      size: pageSize,
      sort: sortOption,
      filters,
    };

    const fetchDepartments = async () => {
  try {
    const data = await getDepartments();
    setDepartments(data);
  } catch {
    message.error("Departmanlar yüklenemedi.");
  }
};

    const data = await filterAttendance(request);

    // Bu cevap, en son gönderdiğimiz istekten değilse (yani araya
    // daha yeni bir istek girdiyse), bu cevabı görmezden gel.
    if (currentRequestId !== requestIdRef.current) return;

    setAttendanceList(data.content);
    setTotalElements(data.totalElements);
  } catch {
    message.error("Attendance kayıtları yüklenemedi.");
  } finally {
    if (currentRequestId === requestIdRef.current) {
      setLoading(false);
    }
  }
};
    const handleLogout = async () => {
      if (hasActiveAttendance) {
        try {
          await checkOutAttendance();
          message.success("Aktif girişiniz otomatik olarak kapatıldı.");
        } catch {
          message.error("Çıkış işlemi sırasında bir hata oluştu, yine de oturumunuz kapatılıyor.");
        }
      }
      logout();
    };

    const handleSortClick = (field: string) => {
  const [prevField, prevDir] = sortOption.split(",");
  if (prevField === field) {
    setSortOption(`${field},${prevDir === "asc" ? "desc" : "asc"}`);
  } else {
    setSortOption(`${field},desc`);
  }
  setPage(0);
};

const renderSortableTitle = (label: string, field: string) => {
  const [sortField, sortDir] = sortOption.split(",");
  const isActive = sortField === field;
;


  return (
    <span
      style={{
        cursor: "pointer",
        userSelect: "none",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        width: "100%",
      }}
      onClick={() => handleSortClick(field)}
    >
      {label}
      <span style={{ display: "inline-flex", flexDirection: "column", lineHeight: 0, marginLeft: 8 }}>
        <CaretUpOutlined
          style={{
            fontSize: 11,
            color: isActive && sortDir === "asc" ? "#2f6fed" : "#bfbfbf",
          }}
        />
        <CaretDownOutlined
          style={{
            fontSize: 11,
            marginTop: -2,
            color: isActive && sortDir === "desc" ? "#2f6fed" : "#bfbfbf",
          }}
        />
      </span>
    </span>
  );
};

 const columns: ColumnsType<AttendanceResponse> = [
  {
    title: renderSortableTitle("Giriş Saati", "loginTime"),
    dataIndex: "loginTime",
    key: "loginTime",
    render: (value: string) => new Date(value).toLocaleString("tr-TR"),
  },
  {
    title: "Çıkış Saati",
    dataIndex: "logoutTime",
    key: "logoutTime",
    render: (value: string | null) =>
      value ? new Date(value).toLocaleString("tr-TR") : "-",
  },
  {
    title: renderSortableTitle("Çalışılan Süre (saat)", "workedHours"),
    dataIndex: "workedHours",
    key: "workedHours",
    render: (value: number | null) => (value !== null ? value.toFixed(2) : "-"),
  },
  {
    title: "Durum",
    dataIndex: "status",
    key: "status",
    render: (value: string) => (value === "ACTIVE" ? "Aktif" : "Tamamlandı"),
  },
  {
  title: "Geç Giriş",
  dataIndex: "isLate",
  key: "isLate",
  render: (v: boolean) => (v ? <Tag color="red">Geç</Tag> : <Tag color="green">Zamanında</Tag>),
  },
  {
    title: "Erken Çıkış",
    dataIndex: "isEarlyLeave",
    key: "isEarlyLeave",
    render: (v: boolean) => (v ? <Tag color="orange">Erken</Tag> : <Tag color="default">-</Tag>),
  },
  {
    title: "Fazla Mesai",
    dataIndex: "overtimeMinutes",
    key: "overtimeMinutes",
    render: (v: number | null) => (v && v > 0 ? <Tag color="blue">{(v / 60).toFixed(2)} saat</Tag> : "-"),
  },
];

  const leaveColumns: ColumnsType<LeaveRequestResponse> = [
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
]

const handleTableChange = (pagination: any) => {
  if (pagination.current) setPage(pagination.current - 1);
  if (pagination.pageSize) setPageSize(pagination.pageSize);
};

  const formatDate = (isoDate: string) => {
  const d = new Date(isoDate);
  return d.toLocaleDateString("tr-TR", { day: "2-digit", month: "2-digit", year: "numeric" });
  };

  const chartData = weeklyChart.map((d) => ({
  day: d.day,
  hours: d.hours,
  status: d.isLeave ? "İzinli" : "Çalışma",
  }));


  return (
    <div style={{ maxWidth: 1000, margin: "40px auto", padding: "0 20px" }}>
      <Row justify="space-between" align="middle" style={{ marginBottom: 24 }}>
        <h2>Hoş geldin, {user?.firstName}!</h2>

        <Space>
          {user?.role === "MANAGER" && (
            <Button type="primary" onClick={() => navigate("/manager/team")}>
              Ekibim
            </Button>
          )}

          <Button onClick={handleLogout}>
            Çıkış Yap
          </Button>
        </Space>
      </Row>
      <Row style={{ marginBottom: 24 }}>
  {hasActiveAttendance ? (
    <Button type="primary" danger onClick={handleCheckOut}>
      Oturumu Bitir
    </Button>
    ) : (
    <Button type="primary" onClick={() => navigate("/qr-scan")}>
      Karekod Okut
    </Button>
    )}
    </Row>

      {/* Stat Kartları */}
      <Row gutter={[16,16]} style={{ marginBottom: 24}}>
        <Col span={6}>
          <Card style={{ height: "100%" }}>
            <Statistic
              title="Bugünkü Süre" 
              value={stats?.todayWorkedHours ?? 0} 
              suffix="s" 
              precision={2}
            />
          </Card>
        </Col>

        <Col span={6}>
          <Card style={{ height: "100%" }}>
            <Statistic
              title="Haftalık Süre"
              value={stats?.weeklyWorkedHours ?? 0}
              suffix="s"
              precision={2}
            />
          </Card>
        </Col>

        <Col span={6}>
          <Card style={{ height: "100%" }}>
            <Statistic
              title="Haftalık Giriş"
              value={stats?.weeklyCheckInCount ?? 0}
            />
          </Card>
        </Col>

        <Col span={6}>
          <Card style={{ height: "100%" }}>
            <Statistic
              title="Haftalık Çıkış"
              value={stats?.weeklyCheckOutCount ?? 0}
            />
          </Card>
        </Col>

        <Col span={6}>
          <Card style={{ height: "100%" }}>
            <Statistic
              title="Toplam Süre"
              value={stats?.totalWorkedHours ?? 0}
              suffix="s"
              precision={2}
            />
          </Card>
        </Col>
          <Col span={6}>
            <Card style={{ height: "100%" }}>
            <Statistic title="Bu Hafta İzinli Gün" value={stats?.weeklyLeaveDayCount ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ height: "100%" }}>
            <Statistic title="Bu Ay Geç Kalma" value={stats?.monthlyLateCount ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ height: "100%" }}>
            <Statistic title="Bu Ay Fazla Mesai (saat)" value={stats?.monthlyOvertimeHours ?? 0} precision={2} />
          </Card>
        </Col>
      </Row>

          <Card style={{ marginBottom: 24 }}>
            <Row justify="space-between" align="middle" style={{ marginBottom: 4 }}>
            <h3 style={{ margin: 0 }}>Haftalık Çalışma Saatleri</h3>
            <Row gutter={8} align="middle">
              <Button size="small" onClick={() => setWeekOffset((prev) => prev - 1)}>
                ← Önceki Hafta
              </Button>
              <Button
                size="small"
                onClick={() => setWeekOffset((prev) => prev + 1)}
                disabled={weekOffset >= 0}
                style={{ marginLeft: 8 }}
              >
                Sonraki Hafta →
              </Button>
            </Row>
          </Row>

          <Row justify="space-between" align="middle" style={{ marginBottom: 12 }}>
            <span style={{ color: "#6b7280", fontSize: 13 }}>
              {weekRange && `${formatDate(weekRange.start)} — ${formatDate(weekRange.end)}`}
            </span>
            {weekOffset !== 0 && (
              <Button size="small" type="link" onClick={() => setWeekOffset(0)}>
                Bu Hafta
              </Button>
            )}
          </Row>

          <Column
            data={chartData}
            xField="day"
            yField="hours"
            colorField="status"
            height={250}
            tooltip={{
              formatter: (datum: { day: string; hours: number; status: string }) => ({
                name: datum.status,
                value: datum.status === "İzinli" ? "İzinli" : `${datum.hours} saat`,
              }),
            }}
          />
        </Card>

        <Card style={{ marginBottom: 24 }}>
  <Row gutter={16}>
    <Col>
      <Select
        placeholder="Duruma göre filtrele"
        style={{ width: 200 }}
        allowClear
        value={statusFilter}
        onChange={(value) => {
          setStatusFilter(value);
          setPage(0);
        }}
        options={[
          { value: "ACTIVE", label: "Aktif" },
          { value: "COMPLETED", label: "Tamamlandı" },
        ]}
      />
    </Col>
    <Col>
      <RangePicker
        format="DD/MM/YYYY"
        disabledDate={(current) => current && current > dayjs().endOf("day")}
        onChange={(dates) => {
          if (dates && dates[0] && dates[1]) {
            if (dates[0].isAfter(dayjs().endOf("day")) || dates[1].isAfter(dayjs().endOf("day"))) {
              message.error("Geçerli bir tarih aralığı giriniz.");
              setDateRange(null);
              return;
            }
            setDateRange([dates[0].format("YYYY-MM-DD"), dates[1].format("YYYY-MM-DD")]);
          } else {
            setDateRange(null);
          }
          setPage(0);
        }}
      />
    </Col>

    <Col>
      <Select
        placeholder="Geç kalma durumu"
        style={{ width: 170 }}
        allowClear
        value={lateFilter}
        onChange={(value) => {
          setLateFilter(value);
          setPage(0);
        }}
        options={[
          { value: "true", label: "Sadece Geç Kalanlar" },
          { value: "false", label: "Sadece Zamanında" },
        ]}
      />
    </Col>

    <Col>
      <Select
        style={{ width: 200 }}
        value={sortOption}
        onChange={(value) => {
          setSortOption(value);
          setPage(0);
        }}
        options={[
          { value: "loginTime,desc", label: "Giriş: Yeniden Eskiye" },
          { value: "loginTime,asc", label: "Giriş: Eskiden Yeniye" },
          { value: "workedHours,desc", label: "Çalışma Süresi: Çoktan Aza" },
          { value: "workedHours,asc", label: "Çalışma Süresi: Azdan Çoğa" },
        ]}
      />
    </Col>
  </Row>
</Card>
    

      {/* Attendance Tablosu */}
      <Card>
        {loading ? (
          <Spin />
        ) : (
          <Table
            columns={columns}
            dataSource={attendanceList}
            rowKey="id"
            pagination={{
              current: page + 1,
              pageSize: pageSize,
              total: totalElements,
              pageSizeOptions: ["5", "10", "20", "50"],
              showSizeChanger: true,
              showTotal: (total, range) => `${range[0]}-${range[1]} / ${total} kayıt`,
            }}
            onChange={handleTableChange}
          />
        )}
      </Card>

      <Card style={{ marginTop: 24 }}>
        <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
          <h3 style={{ margin: 0 }}>İzinlerim</h3>
          <Button type="primary" onClick={() => setLeaveModalOpen(true)}>
            İzin Talebi Oluştur
          </Button>
        </Row>
        <Table columns={leaveColumns} dataSource={myLeaves} rowKey="id" pagination={false} />
      </Card>

      <Modal
        title="İzin Talebi Oluştur"
        open={leaveModalOpen}
        onOk={handleLeaveSubmit}
        onCancel={() => setLeaveModalOpen(false)}
      >
        <Form form={leaveForm} layout="vertical">
          <Form.Item name="dateRange" label="Tarih Aralığı" rules={[{ required: true, message: "Tarih aralığı seçiniz" }]}>
            <RangePicker
              format="DD/MM/YYYY"
              disabledDate={(current) => current && current < dayjs().startOf("day")}
            />
          </Form.Item>
          <Form.Item name="leaveType" label="İzin Türü" rules={[{ required: true }]}>
            <Select
              options={[
                { value: "ANNUAL", label: "Yıllık İzin" },
                { value: "EXCUSE", label: "Mazeret İzni" },
                { value: "SICK", label: "Rapor" },
              ]}
            />
          </Form.Item>
          <Form.Item name="reason" label="Açıklama (opsiyonel)">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>

    
  );
  
};

export default DashboardPage;