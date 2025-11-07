{{- define "ntdoc-core.name" -}}{{ .Chart.Name }}{{- end -}}
{{- define "ntdoc-core.fullname" -}}{{ printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}{{- end -}}
{{- define "ntdoc-core.labels" -}}
app.kubernetes.io/name: {{ include "ntdoc-core.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
{{- define "ntdoc-core.selectorLabels" -}}
app.kubernetes.io/name: {{ include "ntdoc-core.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
{{- define "ntdoc-core.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{- default (printf "%s-%s" .Release.Name .Chart.Name) .Values.serviceAccount.name -}}
{{- else -}}
default
{{- end -}}
{{- end -}}
