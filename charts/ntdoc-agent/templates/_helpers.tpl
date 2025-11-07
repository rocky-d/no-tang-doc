{{- define "ntdoc-agent.name" -}}{{ .Chart.Name }}{{- end -}}
{{- define "ntdoc-agent.fullname" -}}{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}{{- end -}}
{{- define "ntdoc-agent.labels" -}}
app.kubernetes.io/name: {{ include "ntdoc-agent.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
{{- define "ntdoc-agent.selectorLabels" -}}
app.kubernetes.io/name: {{ include "ntdoc-agent.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
{{- define "ntdoc-agent.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{- default (printf "%s-%s" .Release.Name .Chart.Name) .Values.serviceAccount.name -}}
{{- else -}}
default
{{- end -}}
{{- end -}}
