import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerUser } from "../service/authApi";
import "./Auth.css";

const RegisterPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setError(null);
  setLoading(true);

  try {
    await registerUser(formData);
    navigate("/login");
  } catch (err: any) {
    const data = err.response?.data;

    if (data?.fieldErrors) {
      // birden fazla alan hatası olabilir, hepsini birleştir
      const messages = Object.values(data.fieldErrors) as string[];
      setError(messages.join(" "));
    } else if (data?.message) {
      setError(data.message);
    } else {
      setError("Kayıt sırasında bir hata oluştu.");
    }
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="auth-page">
      <div className="auth-logo">
        i<span>NFODİF</span> ICard
      </div>

      <div className="auth-card">
        <h2 className="auth-title">Kayıt Ol</h2>
        <p className="auth-sub">Sisteme erişmek için hesap oluşturun.</p>

        <form onSubmit={handleSubmit}>
          <div className="field-row">
            <div className="field-group">
              <label className="field-label" htmlFor="firstName">
                Ad
              </label>
              <input
                id="firstName"
                className="field-input"
                name="firstName"
                placeholder="Ahmet"
                value={formData.firstName}
                onChange={handleChange}
                required
              />
            </div>
            <div className="field-group">
              <label className="field-label" htmlFor="lastName">
                Soyad
              </label>
              <input
                id="lastName"
                className="field-input"
                name="lastName"
                placeholder="Yılmaz"
                value={formData.lastName}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="field-group">
            <label className="field-label" htmlFor="email">
              Email
            </label>
            <input
              id="email"
              className="field-input"
              name="email"
              type="email"
              placeholder="ornek@sirket.com"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="field-group">
            <label className="field-label" htmlFor="password">
              Şifre
            </label>
            <input
              id="password"
              className="field-input"
              name="password"
              type="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" className="auth-submit" disabled={loading}>
            {loading ? "Kaydediliyor..." : "Kayıt Ol"}
          </button>
        </form>

        <p className="auth-switch">
          Zaten hesabın var mı? <Link to="/login">Giriş Yap</Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;