import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { loginUser } from "../service/authApi";
import { useAuth } from "../context/AuthContext";
import "./Auth.css";

const LoginPage = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState({ email: "", password: "" });
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
    const response = await loginUser(formData);
    login(response.token, response.user);
    if (response.user.role === "MANAGER") {
      navigate("/manager/team");
    }
    if (response.user.role === "ADMIN"){
      navigate("/admin")
    }
     else {
      navigate("/dashboard");
    }
  } catch (err: any) {
    const data = err.response?.data;

    if (data?.fieldErrors) {
      const messages = Object.values(data.fieldErrors) as string[];
      setError(messages.join(" "));
    } else if (data?.message) {
      setError(data.message);
    } else {
      setError("Email veya şifre hatalı.");
    }
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="auth-page">
      <div className="auth-logo">
        i<span>NFODIF</span> ICard
      </div>

      <div className="auth-card">
        <h2 className="auth-title">Giriş Yap</h2>
        <p className="auth-sub">Devam etmek için giriş yapınız.</p>

        <form onSubmit={handleSubmit}>
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
            {loading ? "Giriş yapılıyor..." : "Giriş Yap"}
          </button>
        </form>

        <p className="auth-switch">
          Hesabın yok mu? <Link to="/register">Kayıt Ol</Link>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;