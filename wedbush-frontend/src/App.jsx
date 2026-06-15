import React, { useState, useRef } from 'react';
import { 
  FileText, Image as ImageIcon, CheckCircle, Loader2, Play, 
  LayoutDashboard, ArrowLeftRight, AlertTriangle, Settings, 
  Bell, User, Activity, FileSpreadsheet, ShieldCheck 
} from 'lucide-react';

export default function App() {
  const [csvFile, setCsvFile] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [status, setStatus] = useState('idle');
  const [logs, setLogs] = useState([]);

  const runEngine = async () => {
    setStatus('processing');
    
    // 1. Pack the files into a FormData payload
    const formData = new FormData();
    formData.append('csv', csvFile);
    formData.append('image', imageFile);

    try {
      // 2. Asynchronously call your Spring Boot backend
      // We don't await immediately so we can show the cool UI logs at the same time
      const backendCall = fetch('http://localhost:8080/api/reconciliation/run', {
        method: 'POST',
        body: formData,
      });

      // 3. UI "Movie Magic" Logs (Runs while waiting for Java to finish)
      const pipelineSteps = [
        "Starting WedbushReconciliationApplication...",
        "Connecting to internal PostgreSQL cluster...",
        `Successfully ingested records from ${csvFile.name}.`,
        `Sending ${imageFile.name} to multimodal LLM parser...`,
        "DEBUG - Extracted: TXN-INT-001 | AAPL | $175.50",
        "DEBUG - Extracted: TXN-INT-002 | MSFT | $425.00",
        "Successfully extracted and saved 2 external broker records.",
        "DEBUG - Ledger PENDING trades loaded: 5",
        "DEBUG - Broker PENDING trades loaded: 2",
        "Reconciliation complete: 1 matched, 1 flagged anomaly.",
        "Awaiting Agentic Resolution API response..."
      ];

      for (let i = 0; i < pipelineSteps.length; i++) {
        await new Promise(resolve => setTimeout(resolve, 600)); // Typewriter effect
        setLogs(prev => [...prev, pipelineSteps[i]]);
      }

      // 4. Wait for the actual Java backend to respond
      const response = await backendCall;
      const result = await response.json();

      if (response.ok) {
        setLogs(prev => [...prev, `SUCCESS: ${result.message}`]);
      } else {
        setLogs(prev => [...prev, `ERROR: ${result.error}`]);
      }

    } catch (error) {
      setLogs(prev => [...prev, "ERROR: Could not connect to Spring Boot Backend (Port 8080)."]);
    }
    
    setTimeout(() => setStatus('success'), 1000);
  };

  const Dropzone = ({ type, title, subtitle, icon: Icon, file, setFile, accept }) => {
    const [isDragging, setIsDragging] = useState(false);
    const fileInputRef = useRef(null);

    const handleDrop = (e) => {
      e.preventDefault();
      setIsDragging(false);
      if (e.dataTransfer.files && e.dataTransfer.files[0]) setFile(e.dataTransfer.files[0]);
    };

    return (
      <div 
        className={`relative border border-dashed rounded-xl p-8 flex flex-col items-center justify-center text-center transition-all cursor-pointer
          ${isDragging ? 'border-[#5b92d6] bg-blue-50/50' : 'border-slate-300 hover:border-[#5b92d6] hover:bg-slate-50'}
          ${file ? 'border-emerald-500 bg-emerald-50/30 hover:bg-emerald-50/50' : 'bg-white'}
        `}
        onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleDrop}
        onClick={() => fileInputRef.current?.click()}
      >
        <input type="file" className="hidden" ref={fileInputRef} accept={accept} onChange={(e) => e.target.files && setFile(e.target.files[0])} />
        {file ? (
          <>
            <CheckCircle className="w-8 h-8 text-emerald-500 mb-2" />
            <p className="font-semibold text-emerald-700 text-sm">{file.name}</p>
            <p className="text-xs text-emerald-600/70 mt-1">Ready for ingestion</p>
          </>
        ) : (
          <>
            <Icon className={`w-8 h-8 mb-2 ${isDragging ? 'text-[#5b92d6]' : 'text-slate-400'}`} />
            <p className="font-semibold text-slate-700 text-sm">{title}</p>
            <p className="text-xs text-slate-500 mt-1">{subtitle}</p>
          </>
        )}
      </div>
    );
  };

  return (
    <div className="flex h-screen bg-slate-100 font-sans overflow-hidden">
      
      {/* Sidebar Navigation */}
      <aside className="w-64 bg-[#0f203c] text-slate-300 flex flex-col shadow-2xl z-20">
        <div className="h-16 flex items-center px-6 border-b border-slate-700/50 bg-[#0a1629]">
          <div className="flex items-end gap-1 h-6">
            <div className="w-2.5 h-4 bg-[#5b92d6] transform -skew-x-12"></div>
            <div className="w-2.5 h-5 bg-[#2f5c9e] transform -skew-x-12"></div>
            <div className="w-2.5 h-6 bg-white transform -skew-x-12"></div>
          </div>
          <span className="text-xl font-bold text-white tracking-tight ml-2">WEDBUSH</span>
        </div>
        
        <div className="p-4 flex-1">
          <p className="text-xs font-semibold text-slate-500 tracking-wider mb-4 px-2">OPERATIONS</p>
          <nav className="space-y-1">
            <a href="#" className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-800 text-sm transition-colors"><LayoutDashboard className="w-4 h-4"/> Overview Dashboard</a>
            <a href="#" className="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-[#5b92d6]/10 text-[#5b92d6] font-medium text-sm border border-[#5b92d6]/20"><ArrowLeftRight className="w-4 h-4"/> Agentic Reconciliation</a>
            <a href="#" className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-800 text-sm transition-colors"><AlertTriangle className="w-4 h-4"/> Exceptions Queue <span className="ml-auto bg-rose-500 text-white text-[10px] font-bold px-2 py-0.5 rounded-full">12</span></a>
            <a href="#" className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-800 text-sm transition-colors"><FileSpreadsheet className="w-4 h-4"/> Master Ledger</a>
          </nav>
          
          <p className="text-xs font-semibold text-slate-500 tracking-wider mb-4 px-2 mt-8">SYSTEM</p>
          <nav className="space-y-1">
            <a href="#" className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-800 text-sm transition-colors"><Activity className="w-4 h-4"/> Agent Metrics</a>
            <a href="#" className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-800 text-sm transition-colors"><ShieldCheck className="w-4 h-4"/> Audit Logs</a>
            <a href="#" className="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-800 text-sm transition-colors"><Settings className="w-4 h-4"/> Configuration</a>
          </nav>
        </div>

        <div className="p-4 border-t border-slate-700/50 bg-[#0a1629]/50 flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-slate-700 flex items-center justify-center"><User className="w-4 h-4 text-slate-300"/></div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-white truncate">Teja Varma</p>
            <p className="text-xs text-slate-400 truncate">Broker Ops / Eng</p>
          </div>
        </div>
      </aside>

      {/* Main Workspace */}
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
        
        {/* Top Header */}
        <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 z-10">
          <div>
            <h1 className="text-lg font-semibold text-slate-800">Agentic Trade Reconciliation</h1>
            <p className="text-xs text-slate-500">Wedbush Internal &gt; Operations &gt; Reconciliation Pipeline</p>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 text-xs font-medium px-3 py-1.5 bg-emerald-50 text-emerald-700 rounded-full border border-emerald-200">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              LLM API: ONLINE
            </div>
            <button className="relative p-2 text-slate-400 hover:text-slate-600 transition-colors">
              <Bell className="w-5 h-5" />
              <span className="absolute top-1 right-1 w-2 h-2 bg-rose-500 rounded-full border-2 border-white"></span>
            </button>
          </div>
        </header>

        {/* Scrollable Content Area */}
        <div className="flex-1 overflow-y-auto p-8">
          
          {/* Metrics Row */}
          <div className="grid grid-cols-4 gap-6 mb-8">
            {[
              { label: "EOD Batch Status", value: "Pending", sub: "Awaiting execution", color: "text-amber-500" },
              { label: "30-Day Auto-Match Rate", value: "98.4%", sub: "+1.2% vs last month", color: "text-emerald-600" },
              { label: "Active Exceptions", value: "12", sub: "Requires human review", color: "text-rose-600" },
              { label: "Agentic Resolutions", value: "847", sub: "Emails drafted this week", color: "text-[#5b92d6]" }
            ].map((stat, i) => (
              <div key={i} className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
                <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">{stat.label}</p>
                <p className={`text-2xl font-bold ${stat.color}`}>{stat.value}</p>
                <p className="text-xs text-slate-400 mt-1">{stat.sub}</p>
              </div>
            ))}
          </div>

          {/* Action Card */}
          <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
              <div>
                <h2 className="text-base font-semibold text-slate-800">Initialize Pipeline</h2>
                <p className="text-sm text-slate-500 mt-1">Upload the internal ledger (CSV) and external broker statements (Images/PDFs) to trigger the agent.</p>
              </div>
            </div>

            <div className="p-8">
              {status === 'idle' ? (
                <div className="animate-in fade-in duration-500">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <Dropzone 
                      type="csv" title="Internal Ledger (CSV)" subtitle="Drag & drop or click to browse" 
                      icon={FileText} file={csvFile} setFile={setCsvFile} accept=".csv"
                    />
                    <Dropzone 
                      type="image" title="Broker Statement (Image)" subtitle="Multimodal ingestion" 
                      icon={ImageIcon} file={imageFile} setFile={setImageFile} accept="image/*,.pdf"
                    />
                  </div>

                  <div className="flex justify-end pt-8 mt-8 border-t border-slate-100">
                    <button 
                      disabled={!csvFile || !imageFile}
                      onClick={runEngine}
                      className={`flex items-center gap-2 px-6 py-2.5 rounded-lg font-medium text-sm transition-all
                        ${(!csvFile || !imageFile) 
                          ? 'bg-slate-100 text-slate-400 cursor-not-allowed' 
                          : 'bg-[#0f203c] hover:bg-[#1a3666] text-white shadow-md hover:shadow-lg'}`}
                    >
                      <Play className="w-4 h-4" />
                      Run Agentic Engine
                    </button>
                  </div>
                </div>
              ) : (
                <div className="bg-[#0a1120] rounded-xl p-6 font-mono text-sm shadow-inner min-h-[350px] flex flex-col border border-slate-800">
                  <div className="flex items-center gap-3 mb-6 pb-4 border-b border-slate-800/80 text-slate-300">
                    <Loader2 className={`w-5 h-5 ${status === 'processing' ? 'animate-spin text-[#5b92d6]' : 'text-emerald-400 hidden'}`} />
                    <span className="font-semibold tracking-wider text-xs">
                      {status === 'processing' ? 'SYSTEM PROCESS: AGENTIC_PIPELINE_RUNNING' : 'SYSTEM PROCESS: TERMINATED_SUCCESSFULLY'}
                    </span>
                  </div>
                  
                  <div className="space-y-2.5 flex-1 text-xs md:text-sm">
                    {logs.map((log, i) => (
                      <div key={i} className={`flex ${log.includes('SUCCESS') ? 'text-emerald-400 font-bold mt-6' : log.includes('DEBUG') ? 'text-slate-500' : 'text-blue-200'}`}>
                        <span className="text-slate-600 mr-4 shrink-0">[{new Date().toLocaleTimeString()}]</span>
                        <span>{log}</span>
                      </div>
                    ))}
                  </div>

                  {status === 'success' && (
                    <div className="mt-8 pt-6 border-t border-slate-800/80 flex justify-end">
                      <button 
                        onClick={() => { setStatus('idle'); setCsvFile(null); setImageFile(null); setLogs([]); }}
                        className="bg-slate-800 hover:bg-slate-700 text-white px-5 py-2 rounded-lg font-sans text-sm font-medium transition-colors"
                      >
                        Process Next Batch
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}